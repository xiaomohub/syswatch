package org.xiaomo.syswatch.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "alertmanager")
public class AlertmanagerProperties {

    /**
     * Alertmanager服务地址
     */
    private String url;

    /**
     * API版本路径
     */
    private String apiPath = "/api/v2";

    /**
     * 连接超时（毫秒）
     */
    private Integer connectTimeout = 5000;

    /**
     * 读取超时（毫秒）
     */
    private Integer readTimeout = 10000;

    public String getApiBaseUrl() {
        return url + apiPath;
    }

    @PostConstruct
    public void validate() {
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("alertmanager.url must be configured");
        }
    }
}