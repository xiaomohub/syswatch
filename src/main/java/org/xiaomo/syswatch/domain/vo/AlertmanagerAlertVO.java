package org.xiaomo.syswatch.domain.vo;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Alertmanager告警信息VO
 * 对应Alertmanager API返回的alert结构
 */
@Data
public class AlertmanagerAlertVO {

    /**
     * 告警指纹（唯一标识）
     */
    private String fingerprint;

    /**
     * 告警状态：firing, resolved
     */
    private String status;

    /**
     * 告警标签
     */
    private Map<String, String> labels;

    /**
     * 告警注解
     */
    private Map<String, String> annotations;

    /**
     * 告警开始时间
     */
    private OffsetDateTime startsAt;

    /**
     * 告警结束时间
     */
    private OffsetDateTime endsAt;

    /**
     * 告警生成器URL
     */
    private String generatorURL;

    /**
     * 是否被静默
     */
    private Boolean silenced;

    /**
     * 静默ID（如果被静默）
     */
    private String silenceId;

    // ========== 便捷方法 ==========

    public String getAlertName() {
        return labels != null ? labels.get("alertname") : null;
    }

    public String getInstance() {
        return labels != null ? labels.get("instance") : null;
    }

    public String getSeverity() {
        return labels != null ? labels.get("severity") : null;
    }

    public String getSummary() {
        return annotations != null ? annotations.get("summary") : null;
    }

    public String getDescription() {
        return annotations != null ? annotations.get("description") : null;
    }
}