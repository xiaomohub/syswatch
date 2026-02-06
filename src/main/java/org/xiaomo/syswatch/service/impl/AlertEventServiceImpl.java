package org.xiaomo.syswatch.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.xiaomo.syswatch.config.FeishuProperties;
import org.xiaomo.syswatch.domain.dto.AlertDTO;
import org.xiaomo.syswatch.domain.dto.AlertmanagerWebhookDTO;
import org.xiaomo.syswatch.domain.entity.AlertEvent;
import org.xiaomo.syswatch.mapper.AlertEventMapper;
import org.xiaomo.syswatch.service.AlertEventService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class AlertEventServiceImpl implements AlertEventService {

    @Resource
    private AlertEventMapper alertEventMapper;

    @Resource
    private FeishuProperties feishuProperties; // 注入飞书配置

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void handle(AlertmanagerWebhookDTO payload) {
        if (payload == null || payload.getAlerts() == null) {
            return;
        }

        for (AlertDTO alert : payload.getAlerts()) {
            // 1️⃣ 保存数据库
            AlertEvent event = new AlertEvent();
            event.setAlertName(alert.getLabels().get("alertname"));
            event.setSeverity(alert.getLabels().get("severity"));
            event.setStatus(payload.getStatus());

            if (alert.getStartsAt() != null) {
                event.setStartsAt(alert.getStartsAt().toLocalDateTime());
            }
            if (alert.getEndsAt() != null) {
                event.setEndsAt(alert.getEndsAt().toLocalDateTime());
            }

            event.setSummary(alert.getAnnotations().get("summary"));
            event.setDescription(alert.getAnnotations().get("description"));
            event.setLabels(JSON.toJSONString(alert.getLabels()));
            event.setAnnotations(JSON.toJSONString(alert.getAnnotations()));
            event.setCreateTime(LocalDateTime.now());
            event.setUpdateTime(LocalDateTime.now());

            alertEventMapper.insert(event);
            log.info("告警已保存: alertName={}, severity={}", event.getAlertName(), event.getSeverity());

            // 2️⃣ 转发飞书
            try {
                String startsAtStr = "";
                if (alert.getStartsAt() != null) {
                    startsAtStr = alert.getStartsAt().toLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }

                String content = String.format(
                        "**告警类型**: %s\n**告警级别**: %s\n**触发实例**: %s\n**触发时间**: %s\n**告警内容**: %s",
                        alert.getLabels().getOrDefault("alertname", "未知告警"),
                        alert.getLabels().getOrDefault("severity", "unknown"),
                        alert.getLabels().getOrDefault("instance", "unknown"),
                        startsAtStr,
                        alert.getAnnotations().getOrDefault("summary", "")
                );

                // 使用 List<Map> 构建飞书 card，保证序列化符合要求
                List<Map<String, Object>> elements = new ArrayList<>();
                Map<String, Object> element = new HashMap<>();
                element.put("tag", "div");
                element.put("text", Map.of("tag", "lark_md", "content", content));
                elements.add(element);

                Map<String, Object> cardContent = new HashMap<>();
                cardContent.put("config", Map.of("wide_screen_mode", true));
                cardContent.put("elements", elements);

                Map<String, Object> card = new HashMap<>();
                card.put("msg_type", "interactive");
                card.put("card", cardContent);

                // 发送飞书
                ResponseEntity<String> resp = restTemplate.postForEntity(feishuProperties.getWebhook(), card, String.class);
                log.info("发送飞书状态: {}, 响应: {}", resp.getStatusCode(), resp.getBody());

            } catch (Exception e) {
                log.error("发送飞书失败: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public Page<AlertEvent> queryAlertEvents(
            int pageNum,
            int pageSize,
            String alertName,
            String severity,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
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
