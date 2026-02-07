package com.skateboard.podcast.standard.service.container.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FeedWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(final WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(final WebSocketSession session, final CloseStatus status) {
        sessions.remove(session);
    }

    public void broadcast(final String json) {
        for (final WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                sessions.remove(session);
                continue;
            }
            try {
                session.sendMessage(new TextMessage(json));
            } catch (final Exception ignored) {
                sessions.remove(session);
            }
        }
    }
}
