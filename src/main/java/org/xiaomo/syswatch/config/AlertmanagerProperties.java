package org.xiaomo.syswatch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Alertmanager配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "alertmanager")
public class AlertmanagerProperties {

    private String url = "http://localhost:9094";

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

    /**
     * 获取完整的API基础URL
     */
    public String getApiBaseUrl() {
        return url + apiPath;
    }
}