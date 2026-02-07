package com.skateboard.podcast.standard.service.container.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skateboard.podcast.feed.service.application.dto.PostEvent;
import com.skateboard.podcast.feed.service.application.port.out.FeedEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class WebSocketFeedEventPublisher implements FeedEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(WebSocketFeedEventPublisher.class);
    private static final int VERSION = 1;

    private final FeedWebSocketHandler handler;
    private final ObjectMapper objectMapper;

    public WebSocketFeedEventPublisher(
            final FeedWebSocketHandler handler,
            final ObjectMapper objectMapper
    ) {
        this.handler = handler;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishPostEvent(final PostEvent event) {
        if (event == null) {
            return;
        }
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("postId", event.postId().toString());
        payload.put("slug", event.slug());
        payload.put("updatedAt", event.updatedAt().toString());

        broadcast(event.type(), payload);
    }

    @Override
    public void publishFeedUpdated(final Instant updatedAt) {
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("updatedAt", (updatedAt == null ? Instant.now() : updatedAt).toString());
        broadcast("feed.updated", payload);
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
            log.warn("Failed to publish feed event {}", type, e);
        }
    }
}
