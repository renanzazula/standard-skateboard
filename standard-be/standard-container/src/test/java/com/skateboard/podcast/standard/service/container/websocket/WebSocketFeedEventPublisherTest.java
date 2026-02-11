package com.skateboard.podcast.standard.service.container.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skateboard.podcast.feed.service.application.dto.PostEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebSocketFeedEventPublisherTest {

    @Mock
    private FeedWebSocketHandler feedHandler;

    @Mock
    private EventsWebSocketHandler eventsHandler;

    @Test
    void publishPostEvent_emitsExpectedJson() throws Exception {
        final ObjectMapper objectMapper = new ObjectMapper();
        final WebSocketFeedEventPublisher publisher =
                new WebSocketFeedEventPublisher(feedHandler, eventsHandler, objectMapper);

        final UUID postId = UUID.randomUUID();
        final PostEvent event = new PostEvent(
                "post.updated",
                postId,
                "hello-world",
                Instant.parse("2024-01-02T00:00:00Z")
        );

        publisher.publishPostEvent(event);

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(feedHandler, times(1)).broadcast(captor.capture());
        verify(eventsHandler, times(1)).broadcast(captor.capture());

        final JsonNode json = objectMapper.readTree(captor.getValue());
        assertEquals("post.updated", json.get("type").asText());
        assertEquals(1, json.get("version").asInt());
        assertNotNull(json.get("timestamp").asText());
        assertEquals(postId.toString(), json.get("payload").get("postId").asText());
        assertEquals("hello-world", json.get("payload").get("slug").asText());
        assertEquals("2024-01-02T00:00:00Z", json.get("payload").get("updatedAt").asText());
    }

    @Test
    void publishFeedUpdated_emitsExpectedJson() throws Exception {
        final ObjectMapper objectMapper = new ObjectMapper();
        final WebSocketFeedEventPublisher publisher =
                new WebSocketFeedEventPublisher(feedHandler, eventsHandler, objectMapper);

        final Instant updatedAt = Instant.parse("2024-02-01T10:00:00Z");
        publisher.publishFeedUpdated(updatedAt);

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(feedHandler, times(1)).broadcast(captor.capture());
        verify(eventsHandler, times(1)).broadcast(captor.capture());

        final JsonNode json = objectMapper.readTree(captor.getValue());
        assertEquals("feed.updated", json.get("type").asText());
        assertEquals(1, json.get("version").asInt());
        assertNotNull(json.get("timestamp").asText());
        assertEquals("2024-02-01T10:00:00Z", json.get("payload").get("updatedAt").asText());
    }
}
