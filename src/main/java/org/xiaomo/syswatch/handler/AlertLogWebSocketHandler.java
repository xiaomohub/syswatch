package org.xiaomo.syswatch.handler;

import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.concurrent.CopyOnWriteArraySet;

public class AlertLogWebSocketHandler extends TextWebSocketHandler {

    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    // 给所有在线客户端推送日志
    public static void broadcast(String logJson) {
        sessions.forEach(session -> {
            try {
                session.sendMessage(new TextMessage(logJson));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
