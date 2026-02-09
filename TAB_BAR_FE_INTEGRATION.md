# Tab Bar Frontend Integration Guide

This guide explains how the FE should consume the tab bar-related backend
endpoints, cache data, and react to updates.

## Base URLs

The app reads these Expo public env vars:

- `EXPO_PUBLIC_API_BASE_URL`: REST base URL (example: `https://api.example.com/api/v1`).
- `EXPO_PUBLIC_EVENTS_URL` or `EXPO_PUBLIC_WS_URL`: Optional WebSocket URL for events.

## Auth and Session

All authenticated requests use `Authorization: Bearer <accessToken>`.

Role values: `GUEST`, `USER`, `ADMIN`.

Recommended FE flow:

1. Login via `POST /public/auth/login` (or `admin-passcode`, `social`).
2. Store `accessToken`, `refreshToken`, and `expiresInSeconds`.
3. Fetch `GET /users/me` to hydrate `username`/`avatarUrl`.
4. On `401`, call `POST /public/auth/refresh` once and retry the request.
5. On logout, call `POST /public/auth/logout` and clear all cached data.

## Boot Sequence

On app start:

1. Load cached config from storage (if any).
2. Fetch in parallel:
   - `GET /public/app-config`
   - `GET /public/admin-settings`
   - `GET /public/navigation-config`
3. If authenticated, fetch `GET /users/me`.
4. Update local cache and in-memory contexts.

## Navigation Config (Tab Bar)

Fetch current config with `GET /public/navigation-config`:

```json
{
  "tabs": [
    { "id": "index", "name": "Home", "icon": "Home", "order": 0, "enabled": false, "isSystem": true },
    { "id": "feed", "name": "Feed", "icon": "Rss", "order": 1, "enabled": false, "isSystem": true },
    { "id": "skate-square", "name": "Skate Square", "icon": "Droplet", "order": 2, "enabled": false, "isSystem": true },
    { "id": "podcast", "name": "Podcast", "icon": "Mic", "order": 3, "enabled": false, "isSystem": true },
    { "id": "settings", "name": "Settings", "icon": "Settings", "order": 4, "enabled": true, "isSystem": true }
  ]
}
```

FE rules:

- Sort tabs by `order` ascending.
- Standard users: show only `enabled` tabs.
- Admins: show all `enabled` tabs, plus `settings` even if disabled.
- Always render the tab label from `name` and icon from `icon`.

Tab ID to route mapping (system tabs):

- `index` -> `app/(tabs)/index.tsx` (home)
- `feed` -> `app/(tabs)/feed.tsx`
- `skate-square` -> `app/(tabs)/skate-square.tsx`
- `podcast` -> `app/(tabs)/podcast.tsx`
- `settings` -> `app/(tabs)/settings.tsx`

Note: The tab `id` must match the route name under `app/(tabs)`. The canonical
home tab id is `index`.

Admin updates:

- Use `PUT /admin/navigation-config` with the full `tabs` array.
- Preserve `order` as the source of truth.
- Keep `settings` and `index` enabled (server enforces this).

## Admin Settings and App Config

Admin-only screens read from:

- `GET /public/admin-settings`
- `GET /public/app-config`

When admins save changes:

- `PUT /admin/admin-settings` (auth required).
- `PUT /admin/app-config` (auth required).

Validation rules (server enforced):

- At least one auth method enabled.
- At least one language enabled.
- `defaultCode` must be in `availableCodes`.
- `usernameMaxLength` must be greater than `usernameMinLength`.

## User Management (Admin)

Admin-only screen integration:

- `GET /admin/users` to list users.
- `PATCH /admin/users/{id}` to update name, username, role, or status.
- `DELETE /admin/users/{id}` to remove a user.

Rule: server blocks disabling or deleting the currently authenticated admin.

## User Profile

Use the profile endpoint to hydrate and update username/avatar data.

- `GET /users/me` returns the full profile (includes `username`).
- `PUT /users/me` updates `name`, `username`, and `avatarUrl` (auth required).

Response example:

```json
{
  "id": "user_1",
  "email": "user@example.com",
  "role": "USER",
  "provider": "GOOGLE",
  "name": "Rory",
  "username": "rory",
  "avatarUrl": "https://cdn.example.com/avatar.png"
}
```

## WebSocket Events (Optional)

If events are enabled, connect to `EXPO_PUBLIC_EVENTS_URL` and refresh cached data
when these events arrive:

- `admin-settings.updated` -> refetch `/public/admin-settings`
- `navigation.updated` -> refetch `/public/navigation-config`
- `app-config.updated` -> refetch `/public/app-config`

## Error Handling

Use status codes to drive UX:

- `400`: show validation details from response.
- `401`: attempt refresh; if it fails, force logout.
- `403`: show not authorized.
- `409`: show conflict message (duplicate or invalid state).

All error responses are JSON with a `message` field.
