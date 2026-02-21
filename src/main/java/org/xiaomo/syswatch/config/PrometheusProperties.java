package org.xiaomo.syswatch.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "prometheus")
public class PrometheusProperties {

    /**
     * Prometheus HTTP API 地址
     */
    private String url;

    /**
     * Prometheus 规则目录
     */
    private String ruleDir;

    @PostConstruct
    public void validate() {
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("prometheus.url must be configured");
        }
        if (ruleDir == null || ruleDir.isBlank()) {
            throw new IllegalStateException("prometheus.rule-dir must be configured");
        }
    }
}
