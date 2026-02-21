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
 * 职责：接收 Alertmanager Webhook 回调，持久化告警事件，并转发至飞书群通知。
 * <p>
 * 注意：静默功能由 Alertmanager 自身处理，被静默的告警不会发送到 Webhook，
 * 因此这里不需要再做静默检查。
 */
@Slf4j
@Service
public class AlertEventServiceImpl implements AlertEventService {

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
    //  单条告警处理
    // ============================

    private void processOneAlert(AlertmanagerWebhookDTO payload, AlertDTO alert) {
        String alertName = alert.getLabels().get("alertname");
        String instance  = alert.getLabels().get("instance");
        String status    = alert.getStatus();

        // 1. 根据状态持久化
        String timeLabel;
        String timeValue;

        if ("firing".equals(status)) {
            LocalDateTime firingTime = saveFiringEvent(alert, alertName);
            timeLabel = "触发时间";
            timeValue = firingTime != null ? firingTime.format(DATETIME_FMT) : "";
        } else if ("resolved".equals(status)) {
            LocalDateTime resolvedTime = resolveEvent(alertName, instance);
            timeLabel = "恢复时间";
            timeValue = resolvedTime != null ? resolvedTime.format(DATETIME_FMT) : "";
        } else {
            timeLabel = "事件时间";
            timeValue = LocalDateTime.now(CHINA_ZONE).format(DATETIME_FMT);
        }

        // 2. 查询规则阈值，拼接展示名称
        String displayAlertName = buildDisplayAlertName(alertName);

        // 3. 转发飞书通知
        sendFeishuNotification(payload, alert, displayAlertName, timeLabel, timeValue);
    }

    // ============================
    //  持久化：firing 插入
    // ============================

    private LocalDateTime saveFiringEvent(AlertDTO alert, String alertName) {
        LocalDateTime now = LocalDateTime.now(CHINA_ZONE);
        LocalDateTime chinaStart = now;

        AlertEvent event = new AlertEvent();
        event.setAlertName(alertName);
        event.setSeverity(alert.getLabels().get("severity"));
        event.setStatus("firing");

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
                        "**%s**: %s\n" +
                        "**告警详情**: %s\n%s\n" +
                        "<at id=all></at>",
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

    private Map<String, Object> buildFeishuCard(String markdownContent) {
        Map<String, Object> textBlock = new HashMap<>();
        textBlock.put("tag", "div");

        Map<String, Object> text = new HashMap<>();
        text.put("tag", "lark_md");
        text.put("content", markdownContent);
        textBlock.put("text", text);

        Map<String, Object> config = new HashMap<>();
        config.put("wide_screen_mode", true);

        Map<String, Object> cardContent = new HashMap<>();
        cardContent.put("config", config);
        cardContent.put("elements", List.of(textBlock));

        Map<String, Object> result = new HashMap<>();
        result.put("msg_type", "interactive");
        result.put("card", cardContent);

        return result;
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