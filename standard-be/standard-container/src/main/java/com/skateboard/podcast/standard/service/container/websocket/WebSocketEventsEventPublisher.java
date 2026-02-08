package com.skateboard.podcast.standard.service.container.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skateboard.podcast.feed.service.events.application.dto.EventEvent;
import com.skateboard.podcast.feed.service.events.application.port.out.EventsEventPublisher;
import com.skateboard.podcast.standard.service.container.appconfig.AppConfigEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class WebSocketEventsEventPublisher implements EventsEventPublisher, AppConfigEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventsEventPublisher.class);
    private static final int VERSION = 1;

    private final EventsWebSocketHandler handler;
    private final ObjectMapper objectMapper;

    public WebSocketEventsEventPublisher(
            final EventsWebSocketHandler handler,
            final ObjectMapper objectMapper
    ) {
        this.handler = handler;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishEventEvent(final EventEvent event) {
        if (event == null) {
            return;
        }
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventId", event.eventId().toString());
        payload.put("slug", event.slug());
        payload.put("updatedAt", event.updatedAt().toString());

        broadcast(event.type(), payload);
    }

    @Override
    public void publishEventsUpdated(final Instant updatedAt) {
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("updatedAt", (updatedAt == null ? Instant.now() : updatedAt).toString());
        broadcast("events.updated", payload);
    }

    @Override
    public void publishConfigUpdated(final Instant updatedAt) {
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("updatedAt", (updatedAt == null ? Instant.now() : updatedAt).toString());
        broadcast("config.updated", payload);
    }

    private void broadcast(final String type, final Map<String, Object> payload) {
        final Map<String, Object> message = new LinkedHashMap<>();
        message.put("type", type);
        message.put("version", VERSION);
        message.put("timestamp", Instant.now().toString());
        message.put("payload", payload);
        try {
            handler.broadcast(objectMapper.writeValueAsString(message));
        } catch (final Exception e) {
            log.warn("Failed to publish event {}", type, e);
        }
    }
}

