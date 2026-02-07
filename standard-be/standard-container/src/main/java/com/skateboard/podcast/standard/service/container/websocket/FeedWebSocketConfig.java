package com.skateboard.podcast.standard.service.container.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class FeedWebSocketConfig implements WebSocketConfigurer {

    private final FeedWebSocketHandler handler;

    public FeedWebSocketConfig(final FeedWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(final WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/feed")
                .setAllowedOrigins("*");
    }
}
