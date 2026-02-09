# Backend Integration Guide

This document describes how the app connects to the backend (REST + WebSocket), including required endpoints, payloads, and environment variables.

## Base URLs and Environment Variables

The app reads these Expo public env vars:

- `EXPO_PUBLIC_API_BASE_URL`: REST API base URL. Defaults to `/api/v1` when unset.
- `EXPO_PUBLIC_EVENTS_URL`: WebSocket endpoint for event change notifications.
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

## Authentication

### Auth response shape

```json
{
  "tokens": {
    "accessToken": "jwt",
    "expiresInSeconds": 900,
    "refreshToken": "jwt",
    "refreshExpiresInSeconds": 5184000
  },
  "user": {
    "id": "user_id",
    "email": "admin@example.com",
    "role": "ADMIN",
    "provider": "PASSCODE",
    "name": "Admin",
    "avatarUrl": "https://example.com/avatar.jpg"
  }
}
```

Note: auth responses return a user summary without `username`. Use `GET /users/me`
to hydrate `username` and `avatarUrl` for profile screens.

### Public auth endpoints

- `POST /public/auth/login`
  - Body:
    ```json
    {
      "provider": "MANUAL",
      "email": "user@example.com",
      "password": "secret",
      "device": { "deviceId": "abc", "deviceName": "iPhone", "platform": "IOS" }
    }
    ```
- `POST /public/auth/register`
  - Body:
    ```json
    {
      "email": "user@example.com",
      "password": "secret",
      "device": { "deviceId": "abc", "deviceName": "Pixel", "platform": "ANDROID" }
    }
    ```
- `POST /public/auth/admin-passcode`
  - Body:
    ```json
    {
      "passcode": "admin123",
      "device": { "deviceId": "abc", "platform": "WEB" }
    }
    ```
- `POST /public/auth/social`
  - Body:
    ```json
    {
      "provider": "GOOGLE",
      "token": "oauth_or_id_token_optional",
      "device": { "deviceId": "abc", "platform": "IOS" }
    }
    ```
- `POST /public/auth/refresh`
  - Body:
    ```json
    { "refreshToken": "jwt", "deviceId": "abc" }
    ```
- `POST /public/auth/logout`
  - Requires `Authorization: Bearer <accessToken>`

### Roles and providers

- Roles: `GUEST`, `USER`, `ADMIN`
- Providers: `MANUAL`, `GOOGLE`, `APPLE`, `FACEBOOK`, `PASSCODE`

## User Profile

- `GET /users/me` returns the full profile (includes `username`).
- `PUT /users/me` updates `name`, `username`, or `avatarUrl` (auth required).

## Events API

### Public

- `GET /public/events?page={page}&size={size}`
  - Returns a page of event summaries.
  - Should include `ETag` and `Last-Modified` headers.
  - If client sends `If-None-Match` / `If-Modified-Since` and data is unchanged, return `304`.

- `GET /public/events/{slug}`
  - Returns full event details including `content` blocks.

### Admin (requires `Authorization: Bearer <accessToken>`)

- `POST /admin/events` (create draft)
- `PUT /admin/events/{id}` (update)
- `POST /admin/events/{id}/publish` (publish)
- `GET /admin/events/{id}` (admin fetch)
- `DELETE /admin/events/{id}`
- `POST /admin/events/import` (bulk import array)
- `POST /admin/events/reset` (reset to defaults)

### Event payloads

Event summary:

```json
{
  "id": "evt_123",
  "title": "Street Jam Finals",
  "slug": "street-jam-finals",
  "excerpt": "Finals with the top skaters.",
  "tags": ["street", "finals"],
  "status": "PUBLISHED",
  "thumbnail": { "url": "https://cdn.example.com/cover.jpg", "alt": "Event cover" },
  "startAt": "2026-05-12T19:30:00Z",
  "endAt": "2026-05-12T22:30:00Z",
  "timezone": "UTC",
  "location": "Plaza Skatepark, Barcelona",
  "ticketsUrl": "https://tickets.example.com/event",
  "createdAt": "2026-02-07T12:00:00Z",
  "updatedAt": "2026-02-07T12:34:56Z",
  "createdBy": "admin_1"
}
```

Event details is the same plus `content` blocks:

```json
{
  "content": [
    { "type": "paragraph", "text": "Welcome to the finals." },
    { "type": "image", "image": { "url": "https://cdn.example.com/photo.jpg" } }
  ]
}
```

Block types supported by the app:

- `paragraph`: `{ "type": "paragraph", "text": "...", "html": "<p>...</p>" }`
- `heading`: `{ "type": "heading", "level": 1, "text": "..." }`
- `image`: `{ "type": "image", "image": { "url": "...", "alt": "..." } }`
- `quote`: `{ "type": "quote", "text": "...", "author": "..." }`
- `video`: `{ "type": "video", "url": "...", "poster": "..." }`
- `embed`: `{ "type": "embed", "platform": "youtube", "id": "..." }`
- `gallery`: `{ "type": "gallery", "urls": ["...", "..."] }`
- `link`: `{ "type": "link", "url": "...", "title": "...", "description": "..." }`
- `spotify`: `{ "type": "spotify", "url": "...", "spotifyType": "track", "spotifyId": "..." }`

Notes:
- The app sends base64 Data URLs for uploaded images (e.g. `data:image/jpeg;base64,...`). The backend should accept either hosted URLs or data URLs.
- `startAt` and `endAt` are ISO-8601 strings.

## WebSocket Events

The app subscribes to a plain JSON WebSocket stream:

- URL: `wss://<host>/ws/events` (or `ws://...` for local)
- Env var: `EXPO_PUBLIC_EVENTS_URL` (fallback `EXPO_PUBLIC_WS_URL`)

The client refreshes when the message `type` (or payload `type`) contains `event`, `events`, or `content`.

Example payload:

```json
{
  "type": "event.updated",
  "version": 1,
  "timestamp": "2026-02-07T12:34:56.000Z",
  "payload": {
    "eventId": "123",
    "slug": "new-event",
    "updatedAt": "2026-02-07T12:34:56.000Z"
  }
}
```

## Related Docs

- `docs/openapi.yaml` for a full schema reference.
- `docs/BACKEND_EVENTS_JAVA.md` for a Spring Boot implementation example.
