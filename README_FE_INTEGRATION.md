# Frontend Integration Notes

This guide focuses on the FE changes needed after the unified feed update.

## Base URLs

The app reads these Expo public env vars:

- `EXPO_PUBLIC_API_BASE_URL`: REST base URL (defaults to `/api/v1` when unset).
- `EXPO_PUBLIC_EVENTS_URL`: WebSocket endpoint for events updates.
- `EXPO_PUBLIC_WS_URL`: Fallback WebSocket URL if `EXPO_PUBLIC_EVENTS_URL` is not set.

Examples:

```bash
# local (simulator)
EXPO_PUBLIC_API_BASE_URL=http://localhost:8080/api/v1
EXPO_PUBLIC_EVENTS_URL=ws://localhost:8080/ws/events

# local (device: use LAN IP)
EXPO_PUBLIC_API_BASE_URL=http://192.168.0.17:8080/api/v1
EXPO_PUBLIC_EVENTS_URL=ws://192.168.0.17:8080/ws/events

# prod
EXPO_PUBLIC_API_BASE_URL=https://api.example.com/api/v1
EXPO_PUBLIC_EVENTS_URL=wss://api.example.com/ws/events
```

## Public feed (posts + events)

`GET /public/feed` now returns a mixed feed of posts and events:

```json
{
  "items": [
    {
      "type": "POST",
      "slug": "new-post",
      "publishedAt": "2026-01-01T12:00:00Z"
    },
    {
      "type": "EVENT",
      "slug": "street-jam",
      "startAt": "2026-02-10T18:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalItems": 2
}
```

Important fields:

- `type`: `POST` or `EVENT`.
- Common fields: `id`, `title`, `slug`, `excerpt`, `tags`, `status`, `thumbnail`.
- Post-only: `publishedAt`.
- Event-only: `startAt`, `endAt`, `timezone`, `location`, `ticketsUrl`, `createdAt`, `updatedAt`, `createdBy`.

Use `type` to decide which detail endpoint to call:

- `POST` -> `GET /public/posts/{slug}`
- `EVENT` -> `GET /public/events/{slug}`

## Caching

`/public/feed` and `/public/events` return `ETag` + `Last-Modified` headers.
Send `If-None-Match` or `If-Modified-Since` to get a `304` when nothing changed.

## WebSocket updates

- Feed updates: `WS /ws/feed` (types containing `feed` or `post`).
- Event updates: `WS /ws/events` (types containing `event` or `events`).

When receiving an event update, refresh `/public/feed` to keep the mixed feed in sync.
