package org.xiaomo.syswatch.domain.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Alertmanager Webhook DTO
 *
 * @author xiaomo
 */
@Data
public class AlertmanagerWebhookDTO {

    /**
     * firing / resolved
     */
    private String status;

    /**
     * 告警接收器名称
     */
    private String receiver;

    /**
     * 告警列表
     */
    private List<AlertDTO> alerts;

    /**
     * 分组标签（用于告警聚合）
     */
    private Map<String, String> groupLabels;

    /**
     * 公共标签
     */
    private Map<String, String> commonLabels;

    /**
     * 公共注解
     */
    private Map<String, String> commonAnnotations;

}