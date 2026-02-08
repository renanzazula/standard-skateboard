package com.skateboard.podcast.standard.service.container.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class EventsWebSocketConfig implements WebSocketConfigurer {

    private final EventsWebSocketHandler handler;

    public EventsWebSocketConfig(final EventsWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(final WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/events")
                .setAllowedOrigins("*");
    }
}
