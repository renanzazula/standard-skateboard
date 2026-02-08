package com.skateboard.podcast.standard.service.container.websocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSocket
public class EventsWebSocketConfig implements WebSocketConfigurer {

    private final EventsWebSocketHandler handler;
    private final List<String> corsAllowedOrigins;
    private final List<String> corsAllowedOriginPatterns;

    public EventsWebSocketConfig(
            final EventsWebSocketHandler handler,
            @Value("${app.cors.allowed-origins:}") final String corsAllowedOrigins,
            @Value("${app.cors.allowed-origin-patterns:}") final String corsAllowedOriginPatterns
    ) {
        this.handler = handler;
        this.corsAllowedOrigins = parseCsv(corsAllowedOrigins);
        this.corsAllowedOriginPatterns = parseCsv(corsAllowedOriginPatterns);
    }

    @Override
    public void registerWebSocketHandlers(final WebSocketHandlerRegistry registry) {
        final var registration = registry.addHandler(handler, "/ws/events");
        if (!corsAllowedOriginPatterns.isEmpty()) {
            registration.setAllowedOriginPatterns(corsAllowedOriginPatterns.toArray(new String[0]));
        } else if (!corsAllowedOrigins.isEmpty()) {
            registration.setAllowedOrigins(corsAllowedOrigins.toArray(new String[0]));
        }
    }

    private static List<String> parseCsv(final String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }
}
