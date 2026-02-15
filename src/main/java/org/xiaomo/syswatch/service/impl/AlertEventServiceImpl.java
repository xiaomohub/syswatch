package org.xiaomo.syswatch.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.xiaomo.syswatch.annotation.AlertLog;
import org.xiaomo.syswatch.config.FeishuProperties;
import org.xiaomo.syswatch.domain.dto.AlertDTO;
import org.xiaomo.syswatch.domain.dto.AlertmanagerWebhookDTO;
import org.xiaomo.syswatch.domain.entity.AlertEvent;
import org.xiaomo.syswatch.domain.entity.AlertRule;
import org.xiaomo.syswatch.mapper.AlertEventMapper;
import org.xiaomo.syswatch.mapper.AlertRuleMapper;
import org.xiaomo.syswatch.service.AlertEventService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 告警事件服务实现类
 * <p>
 * 职责：接收 Alertmanager Webhook 回调，持久化告警事件，并转发至飞书群通知（@所有人）。
 */
@Slf4j
@Service
public class AlertEventServiceImpl implements AlertEventService {

    /** 统一使用中国时区 */
    private static final ZoneId CHINA_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private AlertRuleMapper alertRuleMapper;

    @Resource
    private AlertEventMapper alertEventMapper;

    @Resource
    private FeishuProperties feishuProperties;

    private final RestTemplate restTemplate = new RestTemplate();

    // ============================
    //  Webhook 主入口
    // ============================

    @Override
    @AlertLog(action = "告警转发及保存")
    public void handle(AlertmanagerWebhookDTO payload) {
        if (payload == null || payload.getAlerts() == null) {
            return;
        }
        for (AlertDTO alert : payload.getAlerts()) {
            processOneAlert(payload, alert);
        }
    }

    // ============================
    //  单条告警处理（核心流程）
    // ============================

    /**
     * 处理单条告警：持久化 → 查阈值 → 转发飞书
     */
    private void processOneAlert(AlertmanagerWebhookDTO payload, AlertDTO alert) {
        String alertName = alert.getLabels().get("alertname");
        String instance  = alert.getLabels().get("instance");
        String status    = alert.getStatus();

        // 1. 根据状态持久化，并拿到用于飞书展示的时间字符串
        String timeLabel;      // 飞书通知中的时间字段标签
        String timeValue;      // 飞书通知中的时间字段值

        if ("firing".equals(status)) {
            // firing：展示「触发时间」
            LocalDateTime firingTime = saveFiringEvent(alert, alertName);
            timeLabel = "触发时间";
            timeValue = firingTime != null ? firingTime.format(DATETIME_FMT) : "";
        } else if ("resolved".equals(status)) {
            // ⭐ resolved：展示「恢复时间」= 当前中国时间（而非沿用原来的触发时间）
            LocalDateTime resolvedTime = resolveEvent(alertName, instance);
            timeLabel = "恢复时间";
            timeValue = resolvedTime != null ? resolvedTime.format(DATETIME_FMT) : "";
        } else {
            timeLabel = "事件时间";
            timeValue = LocalDateTime.now(CHINA_ZONE).format(DATETIME_FMT);
        }

        // 2. 查询规则阈值，拼接展示名称
        String displayAlertName = buildDisplayAlertName(alertName);

        // 3. 转发飞书通知（@所有人）
        sendFeishuNotification(payload, alert, displayAlertName, timeLabel, timeValue);
    }

    // ============================
    //  持久化：firing 插入
    // ============================

    /**
     * 告警触发时，新增一条 firing 状态的事件记录
     *
     * @return 转换为中国时区后的触发时间（用于飞书展示）
     */
    private LocalDateTime saveFiringEvent(AlertDTO alert, String alertName) {
        LocalDateTime now = LocalDateTime.now(CHINA_ZONE);
        LocalDateTime chinaStart = now; // 默认用当前时间

        AlertEvent event = new AlertEvent();
        event.setAlertName(alertName);
        event.setSeverity(alert.getLabels().get("severity"));
        event.setStatus("firing");

        // 将 Alertmanager 传入的 UTC 时间转为中国时间
        if (alert.getStartsAt() != null) {
            chinaStart = alert.getStartsAt()
                    .atZoneSameInstant(CHINA_ZONE)
                    .toLocalDateTime();
            event.setStartsAt(chinaStart);
        }

        event.setSummary(alert.getAnnotations().get("summary"));
        event.setDescription(alert.getAnnotations().get("description"));
        event.setLabels(JSON.toJSONString(alert.getLabels()));
        event.setAnnotations(JSON.toJSONString(alert.getAnnotations()));
        event.setCreateTime(now);
        event.setUpdateTime(now);

        alertEventMapper.insert(event);
        return chinaStart;
    }

    // ============================
    //  持久化：resolved 更新
    // ============================

    /**
     * 告警恢复时，将对应的 firing 记录标记为 resolved
     *
     * @return 恢复时间（中国时区的当前时间），用于飞书展示
     */
    private LocalDateTime resolveEvent(String alertName, String instance) {
        QueryWrapper<AlertEvent> wrapper = new QueryWrapper<>();
        wrapper.eq("alert_name", alertName)
                .eq("status", "firing")
                .like("labels", instance);

        AlertEvent existing = alertEventMapper.selectOne(wrapper);
        if (existing == null) {
            log.warn("未找到对应的 firing 事件，alertName={}, instance={}", alertName, instance);
            return null;
        }

        // ⭐ 恢复时间 = 当前中国时间（不再沿用原来的 startsAt）
        LocalDateTime now = LocalDateTime.now(CHINA_ZONE);
        existing.setStatus("resolved");
        existing.setEndsAt(now);
        existing.setUpdateTime(now);
        alertEventMapper.updateById(existing);

        return now;
    }

    // ============================
    //  查询告警规则阈值
    // ============================

    /**
     * 根据规则名称查询阈值，拼接用于飞书展示的告警名称
     *
     * @return 例如 "CPU使用率过高 (阈值: 90%)"
     */
    private String buildDisplayAlertName(String alertName) {
        if (alertName == null) {
            return "unknown";
        }

        QueryWrapper<AlertRule> ruleWrapper = new QueryWrapper<>();
        ruleWrapper.eq("rule_name", alertName);
        AlertRule rule = alertRuleMapper.selectOne(ruleWrapper);

        if (rule != null && rule.getThreshold() != null) {
            return alertName + rule.getComparator() + " " + rule.getThreshold();
        }
        return alertName;
    }

    // ============================
    //  飞书通知
    // ============================

    /**
     * 构建飞书卡片消息并发送，末尾 @所有人
     *
     * @param timeLabel 时间字段标签（firing→「触发时间」，resolved→「恢复时间」）
     * @param timeValue 时间字段值（已转为中国时区的格式化字符串）
     */
    private void sendFeishuNotification(AlertmanagerWebhookDTO payload,
                                        AlertDTO alert,
                                        String displayAlertName,
                                        String timeLabel,
                                        String timeValue) {
        try {
            String content = buildFeishuContent(payload, alert, displayAlertName, timeLabel, timeValue);
            Map<String, Object> card = buildFeishuCard(content);

            ResponseEntity<String> resp =
                    restTemplate.postForEntity(feishuProperties.getWebhook(), card, String.class);

            log.info("飞书通知发送成功，状态码: {}, 响应: {}", resp.getStatusCode(), resp.getBody());
        } catch (Exception e) {
            log.error("飞书通知发送失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 构建飞书卡片的 Markdown 正文内容
     * <p>
     * 关键改动：
     *   1. 时区统一转为中国时间
     *   2. resolved 状态展示「恢复时间」而非「触发时间」
     *   3. 末尾追加 @所有人 标签
     */
    private String buildFeishuContent(AlertmanagerWebhookDTO payload,
                                      AlertDTO alert,
                                      String displayAlertName,
                                      String timeLabel,
                                      String timeValue) {
        return String.format(
                "**告警状态**: %s\n" +
                        "**告警类型**: %s\n" +
                        "**告警级别**: %s\n" +
                        "**触发实例**: %s\n" +
                        "**%s**: %s\n" +               // ⭐ 动态标签：firing→触发时间 / resolved→恢复时间
                        "**告警详情**: %s\n%s\n" +
                        "<at id=all></at>",             // ⭐ @所有人
                payload.getStatus(),
                displayAlertName,
                alert.getLabels().getOrDefault("severity", "unknown"),
                alert.getLabels().getOrDefault("instance", "unknown"),
                timeLabel,
                timeValue,
                alert.getAnnotations().getOrDefault("summary", ""),
                alert.getAnnotations().getOrDefault("description", "")
        );
    }

    /**
     * 构建飞书 interactive 卡片结构
     */
    private Map<String, Object> buildFeishuCard(String markdownContent) {
        Map<String, Object> textBlock = Map.of(
                "tag", "div",
                "text", Map.of("tag", "lark_md", "content", markdownContent)
        );

        Map<String, Object> cardContent = Map.of(
                "config", Map.of("wide_screen_mode", true),
                "elements", List.of(textBlock)
        );

        return Map.of(
                "msg_type", "interactive",
                "card", cardContent
        );
    }

    // ============================
    //  分页查询
    // ============================

    @Override
    public Page<AlertEvent> queryAlertEvents(int pageNum,
                                             int pageSize,
                                             String alertName,
                                             String severity,
                                             LocalDateTime startTime,
                                             LocalDateTime endTime) {
        Page<AlertEvent> page = new Page<>(pageNum, pageSize);
        QueryWrapper<AlertEvent> wrapper = new QueryWrapper<>();

        if (alertName != null && !alertName.isEmpty()) {
            wrapper.eq("alert_name", alertName);
        }
        if (severity != null && !severity.isEmpty()) {
            wrapper.eq("severity", severity);
        }
        if (startTime != null) {
            wrapper.ge("starts_at", startTime);
        }
        if (endTime != null) {
            wrapper.le("ends_at", endTime);
        }

        wrapper.orderByDesc("create_time");
        return alertEventMapper.selectPage(page, wrapper);
    }
}