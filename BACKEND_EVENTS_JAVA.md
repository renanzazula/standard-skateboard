# Backend Events + Conditional Feed Sync (Java)

This guide describes a best-practice Java backend implementation for the app's
event-driven feed sync. The goal is to reduce traffic by only syncing when the
backend indicates changes, and to return 304 when nothing changed.

## Overview

Client behavior:
- Subscribes to a WebSocket endpoint set in `EXPO_PUBLIC_EVENTS_URL` (or `EXPO_PUBLIC_WS_URL`).
- When it receives an event with `type` containing `feed` or `post`, it marks the feed
  as dirty and triggers a refresh.
- Refresh uses conditional GET with `If-None-Match` and `If-Modified-Since`.

Backend responsibilities:
1) Emit events when content changes.
2) Support conditional GET for `/public/feed`.

---

## Required endpoints

### GET /public/feed?page={page}&size={size}
Return feed page plus:
- `ETag` header
- `Last-Modified` header

If client sends `If-None-Match` or `If-Modified-Since` and data is unchanged, return:
- `304 Not Modified` (no body)

### WebSocket: wss://<host>/ws/feed
Send plain JSON messages (no STOMP) to all connected clients.

Example payload:
```json
{
  "type": "post.updated",
  "version": 1,
  "timestamp": "2026-02-07T12:34:56.000Z",
  "payload": {
    "postId": "123",
    "slug": "new-episode",
    "updatedAt": "2026-02-07T12:34:56.000Z"
  }
}
```

Event types the app recognizes (any one is enough):
- `feed.updated`
- `post.created`
- `post.updated`
- `post.deleted`
- `post.published`
- `post.unpublished`
- `post.archived`

---

## Implementation plan (Spring Boot)

### 1) Feed versioning strategy

Pick ONE:
- **Simple**: use `max(updated_at)` from posts + total count.
- **Fast**: maintain a `feed_version` table updated on each write.

The client uses `ETag` or `Last-Modified`. Keep it stable across requests.

Example ETag input:
```
etagInput = lastUpdatedAt + ":" + totalCount + ":" + page + ":" + size
```

### 2) Controller with conditional GET

```java
@GetMapping("/public/feed")
public ResponseEntity<PagePostSummary> getFeed(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch,
    @RequestHeader(value = "If-Modified-Since", required = false) String ifModifiedSince
) {
    FeedVersion version = feedService.getFeedVersion(page, size);

    String etag = version.etag();
    String lastModified = version.lastModifiedHttp();

    if (etag.equals(ifNoneMatch) || version.isNotModifiedSince(ifModifiedSince)) {
        return ResponseEntity.status(304)
            .eTag(etag)
            .lastModified(version.lastModifiedEpochMillis())
            .build();
    }

    PagePostSummary data = feedService.getFeedPage(page, size);
    return ResponseEntity.ok()
        .eTag(etag)
        .lastModified(version.lastModifiedEpochMillis())
        .body(data);
}
```

### 3) FeedVersion helper

```java
public record FeedVersion(String etag, Instant lastUpdatedAt) {
    public long lastModifiedEpochMillis() {
        return lastUpdatedAt.toEpochMilli();
    }

    public String lastModifiedHttp() {
        return DateTimeFormatter.RFC_1123_DATE_TIME
            .withZone(ZoneOffset.UTC)
            .format(lastUpdatedAt);
    }

    public boolean isNotModifiedSince(String ifModifiedSince) {
        if (ifModifiedSince == null || ifModifiedSince.isBlank()) return false;
        Instant since = Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(ifModifiedSince));
        return !lastUpdatedAt.isAfter(since);
    }
}
```

### 4) WebSocket broadcaster (plain JSON)

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final FeedWebSocketHandler handler;

    public WebSocketConfig(FeedWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/feed")
            .setAllowedOrigins("*");
    }
}

@Component
public class FeedWebSocketHandler extends TextWebSocketHandler {
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    public void broadcast(String json) {
        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(new TextMessage(json));
            } catch (Exception ignored) {
            }
        }
    }
}
```

### 5) Emit events on content changes

```java
public void onPostChanged(Post post, String type) {
    String json = objectMapper.writeValueAsString(Map.of(
        "type", type,
        "version", 1,
        "timestamp", Instant.now().toString(),
        "payload", Map.of(
            "postId", post.getId(),
            "slug", post.getSlug(),
            "updatedAt", post.getUpdatedAt().toString()
        )
    ));
    feedWebSocketHandler.broadcast(json);
}
```

Call `onPostChanged()` inside create/update/publish/delete flows.

---

## Scaling considerations

If you run multiple instances:
- Use Redis Pub/Sub or Kafka to fan-out events across instances.
- Each instance broadcasts to its connected WebSocket sessions.

---

## Optional: push notifications (background refresh)

Send a data-only message with:
```json
{
  "data": {
    "refresh": true,
    "type": "feed.updated"
  }
}
```

The app uses this to refresh on next open.

---

## Testing checklist

- GET /public/feed returns `ETag` and `Last-Modified`
- Conditional GET returns `304` when unchanged
- WebSocket sends JSON events on post changes
- App updates without manual refresh
