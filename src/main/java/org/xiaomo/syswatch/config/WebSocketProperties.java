package org.xiaomo.syswatch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "syswatch.websocket")
public class WebSocketProperties {
    private List<String> allowedOrigins;
}