package org.xiaomo.syswatch.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 对告警事件进行静默的请求DTO
 */
@Data
public class AlertEventSilenceDTO {

    /**
     * 告警事件ID（本地数据库中的ID）
     */
    private Long alertEventId;

    /**
     * Alertmanager中的告警指纹（fingerprint）
     * 与alertEventId二选一
     */
    private String fingerprint;

    /**
     * 告警名称（直接指定时使用）
     */
    private String alertName;

    /**
     * 实例（直接指定时使用）
     */
    private String instance;

    /**
     * 静默时长（分钟）
     */
    @NotNull(message = "静默时长不能为空")
    @Min(value = 1, message = "静默时长最少1分钟")
    private Integer durationMinutes;

    /**
     * 静默原因/备注
     */
    private String comment;

    /**
     * 操作人
     */
    private String createdBy;
}