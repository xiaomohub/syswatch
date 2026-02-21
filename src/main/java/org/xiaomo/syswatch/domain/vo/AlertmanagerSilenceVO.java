package org.xiaomo.syswatch.domain.vo;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Alertmanager静默信息VO
 * 对应Alertmanager API返回的silence结构
 */
@Data
public class AlertmanagerSilenceVO {

    /**
     * 静默ID
     */
    private String id;

    /**
     * 静默状态：active, pending, expired
     */
    private String status;

    /**
     * 匹配器列表
     */
    private List<Matcher> matchers;

    /**
     * 静默开始时间
     */
    private OffsetDateTime startsAt;

    /**
     * 静默结束时间
     */
    private OffsetDateTime endsAt;

    /**
     * 创建时间
     */
    private OffsetDateTime createdAt;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 备注
     */
    private String comment;

    /**
     * 匹配器
     */
    @Data
    public static class Matcher {
        /**
         * 标签名
         */
        private String name;

        /**
         * 标签值
         */
        private String value;

        /**
         * 是否正则匹配
         */
        private Boolean isRegex;

        /**
         * 是否等于匹配（false表示不等于）
         */
        private Boolean isEqual;
    }
}