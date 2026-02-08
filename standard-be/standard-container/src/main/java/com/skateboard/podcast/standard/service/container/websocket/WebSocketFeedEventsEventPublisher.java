package com.skateboard.podcast.standard.service.container.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skateboard.podcast.feed.service.events.application.dto.FeedEventEvent;
import com.skateboard.podcast.feed.service.events.application.port.out.FeedEventsEventPublisher;
import com.skateboard.podcast.appconfig.service.application.port.out.AppConfigEventPublisher;
import com.skateboard.podcast.appconfig.service.application.port.out.NavigationConfigEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class WebSocketFeedEventsEventPublisher implements FeedEventsEventPublisher, AppConfigEventPublisher, NavigationConfigEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(WebSocketFeedEventsEventPublisher.class);
    private static final int VERSION = 1;

    private final EventsWebSocketHandler handler;
    private final ObjectMapper objectMapper;

    public WebSocketFeedEventsEventPublisher(
            final EventsWebSocketHandler handler,
            final ObjectMapper objectMapper
    ) {
        this.handler = handler;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishFeedEventEvent(final FeedEventEvent event) {
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

    @Override
    public void publishNavigationUpdated(final Instant updatedAt) {
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("updatedAt", (updatedAt == null ? Instant.now() : updatedAt).toString());
        broadcast("navigation.updated", payload);
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

