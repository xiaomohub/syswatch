package org.xiaomo.syswatch.domain.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * 单条告警 DTO
 *
 * @author xiaomo
 */
@Data
public class AlertDTO {

    /**
     * firing / resolved
     */
    private String status;

    /**
     * 标签（alertname / instance / severity 等）
     */
    private Map<String, String> labels;

    /**
     * 注解（summary / description 等）
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
     * 告警指纹（去重关键字段）
     */
    private String fingerprint;
}