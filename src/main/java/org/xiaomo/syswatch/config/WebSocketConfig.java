package org.xiaomo.syswatch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.xiaomo.syswatch.config.WebSocketProperties;
import org.xiaomo.syswatch.handler.AlertLogWebSocketHandler;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final AlertLogWebSocketHandler alertLogWebSocketHandler;
    private final WebSocketProperties webSocketProperties;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(alertLogWebSocketHandler, "/ws/logs")
                .setAllowedOrigins(
                        webSocketProperties.getAllowedOrigins().toArray(new String[0])
                );
    }
}