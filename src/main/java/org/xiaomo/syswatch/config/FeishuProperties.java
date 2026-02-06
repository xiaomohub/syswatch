package org.xiaomo.syswatch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "alert.feishu")
public class FeishuProperties {
    private String webhook;
}
