package org.xiaomo.syswatch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "prometheus")
public class PrometheusProperties {

    /** Prometheus HTTP 接口，用于 reload 或查询 */
    private String url;

    /** Prometheus 规则目录（SSH 写入用） */
    private String ruleDir = "/root/docker-services/prometheus/rules/";
}
