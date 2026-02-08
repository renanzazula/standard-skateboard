# Navigation Management - BE Integration

This document describes how the Navigation Management feature should integrate with the backend.

## Endpoints

### GET /public/navigation-config
Returns the current navigation configuration for the app.

Example response:
```json
{
  "tabs": [
    { "id": "index", "name": "Events", "icon": "Calendar", "order": 0, "enabled": true, "isSystem": true },
    { "id": "settings", "name": "Settings", "icon": "Settings", "order": 1, "enabled": true, "isSystem": true }
  ]
}
```

### PUT /admin/navigation-config
Admin-only update of the navigation configuration.

Request body:
```json
{
  "tabs": [
    { "id": "index", "name": "Events", "icon": "Calendar", "order": 0, "enabled": true, "isSystem": true },
    { "id": "settings", "name": "Admin", "icon": "Settings", "order": 1, "enabled": true, "isSystem": true },
    { "id": "community", "name": "Community", "icon": "LayoutGrid", "order": 2, "enabled": true, "isSystem": false }
  ]
}
```

Notes:
- `settings` tab must remain enabled.
- `id` must be unique and map to a route in `app/(tabs)`.
- `order` is 0-indexed.

## WebSocket Events

When navigation config changes, the backend should emit:
```json
{
  "type": "navigation.updated",
  "timestamp": "2026-02-07T12:34:56.000Z"
}
```

Clients use this to refresh cached navigation settings in the background.

## Caching Strategy (FE)

1. On app start, load cached config from storage.
2. Always call `GET /public/navigation-config` and overwrite the cache.
3. When admin updates config, call `PUT /admin/navigation-config`.
4. On `navigation.updated` WS events, mark config dirty and refresh.

## Authentication

`PUT /admin/navigation-config` requires `Authorization: Bearer <accessToken>`.

## Alignment with UI

The Navigation Management screen should:
- Protect system tabs (settings always enabled).
- Validate unique IDs and non-empty names.
- Show reorder controls and edit/delete actions per the technical guide.
