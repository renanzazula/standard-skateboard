# Settings Navigation - BE Integration

This document describes the backend contract for the Settings-driven navigation configuration used by the app.

## Overview

The app loads navigation configuration from the backend on startup (and caches it locally). Admins can update the
navigation config, which is persisted to the backend and broadcast to clients via WebSocket so they can refresh.

## Endpoints

### Public

- `GET /public/navigation-config`
  - Returns the current navigation configuration.
  - Auth: none.

### Admin

- `PUT /admin/navigation-config`
  - Updates navigation configuration.
  - Auth: `Authorization: Bearer <accessToken>`
  - Body: `NavigationConfig`

## Data Model

```json
{
  "tabs": [
    {
      "id": "index",
      "name": "Feed",
      "icon": "Feed",
      "order": 0,
      "enabled": true,
      "isSystem": true
    },
    {
      "id": "settings",
      "name": "Settings",
      "icon": "Settings",
      "order": 1,
      "enabled": true,
      "isSystem": true
    }
  ]
}
```

Field rules:

- `id`: Tab identifier (also used as the Expo Router route).
- `name`: Tab label in the UI.
- `icon`: Lucide icon name used by FE.
- `order`: 0-based tab order.
- `enabled`: Whether the tab is visible.
- `isSystem`: System tabs cannot be removed; FE forces `settings` to always be enabled.

## Client Behavior

1. App start:
   - Load cached navigation config from local storage.
   - Fetch `GET /public/navigation-config`.
   - Merge and persist the latest config.

2. Admin update:
   - Admin modifies tabs in `/navigation-management`.
   - FE sends `PUT /admin/navigation-config` with full config.

3. Live updates:
   - Backend emits a WebSocket event to notify clients to refresh navigation config.
   - FE marks config dirty and refetches in the background.

## WebSocket Event

Recommended event payload:

```json
{
  "type": "navigation.updated",
  "timestamp": "2026-02-08T20:15:00.000Z"
}
```

The FE listens for event types containing `navigation` or `config` and refetches `GET /public/navigation-config`.

## Notes / Constraints

- The FE currently maps `route = id` for tabs. If the backend needs a different route value, add a `route` field to
  the `NavigationTab` schema and update FE mapping.
- System tabs:
  - `settings` must remain enabled.
  - `index` (Feed) is the default landing tab.
