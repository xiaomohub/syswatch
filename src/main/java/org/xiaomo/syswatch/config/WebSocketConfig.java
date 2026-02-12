package org.xiaomo.syswatch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import org.xiaomo.syswatch.handler.AlertLogWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new AlertLogWebSocketHandler(), "/ws/logs")
                .setAllowedOrigins("*");
    }
}
