# Backend Integration Request

This file summarizes backend work needed to align with the current frontend
endpoint usage and refresh behavior.

## Required

1) Implement `GET /public/settings-config`
   - Public (no auth).
   - Response schema: `AdminSettingsConfig` (same shape as `/admin/admin-settings`).
   - Keep fields: `authMethods`, `authServiceModes`, `sessionConfig`,
     `languageConfig`, `profileConfig`.

2) Keep `PUT /admin/admin-settings`
   - Auth required (bearer).
   - Accepts and returns the same `AdminSettingsConfig` payload.

3) Remove admin passcode login
   - Deprecate `POST /public/auth/admin-passcode`.
   - Use a single login flow (`/public/auth/login`) for all roles.
   - If the endpoint must remain temporarily, return a clear deprecation error.

## Recommended (to match current FE behavior)

4) Role-based navigation and settings
   - `GET /public/navigation-config` should return tabs based on the authenticated user role.
   - `GET /public/settings-config` should return role-appropriate settings.
   - If no auth is provided, return the guest/default view.

5) Realtime events (WebSocket)
   - FE listens on `EXPO_PUBLIC_EVENTS_URL` (typically `/ws/events`).
   - Emit JSON messages with `type` fields containing:
     - `config`, `app-config` for app config changes
     - `settings`, `auth`, `language`, `profile`, `session` for settings changes
     - `feed`, `post`, `event`, `content` for content changes
   - Example payload:
     ```json
     { "type": "app-config.updated", "timestamp": "2026-02-07T12:34:56.000Z" }
     ```

6) Feed cache headers
   - `GET /public/feed` and `GET /public/events` should return `ETag` and
     `Last-Modified`.
   - Support `If-None-Match` and `If-Modified-Since` with `304 Not Modified`.

7) Event detail endpoint should use posts
   - Use `GET /public/posts/{slug}` for both posts and events.
   - If `/public/events/{slug}` exists, keep it as an alias or remove it.

8) Events list should use the feed
   - Use `GET /public/feed?page={page}&size={size}` for mixed posts/events.
   - If `/public/events` exists, keep it as an alias or remove it.

9) Admin events endpoints (if kept)
   - `POST /admin/events`
   - `PUT /admin/events/{id}`
   - `POST /admin/events/{id}/publish`
   - `DELETE /admin/events/{id}`
   - `POST /admin/events/import`
   - `POST /admin/events/reset`

10) Real-time feed updates toggle
   - Add a boolean config flag (e.g. `feedRealtimeEnabled`) in app or settings config.
   - When false, the FE should not open the feed WebSocket.

## Optional compatibility

6) Legacy route alias
   - If `/public/admin-settings` already exists, keep it as an alias
     (or 301) to `/public/settings-config` until clients are updated.
