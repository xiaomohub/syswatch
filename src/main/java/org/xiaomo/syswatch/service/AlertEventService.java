package org.xiaomo.syswatch.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.xiaomo.syswatch.domain.dto.AlertmanagerWebhookDTO;
import org.xiaomo.syswatch.domain.entity.AlertEvent;

import java.time.LocalDateTime;

/**
 * 告警事件处理服务
 *
 * @author xiaomo
 */
public interface AlertEventService {

    /**
     * 处理 Alertmanager Webhook 告警事件
     *
     * @param payload Alertmanager 推送的数据
     */
    void handle(AlertmanagerWebhookDTO payload);

    Page<AlertEvent> queryAlertEvents(
            int pageNum,
            int pageSize,
            String alertName,
            String severity,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
}