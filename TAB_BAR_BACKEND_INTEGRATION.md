# Tab Bar Backend Integration Guide

This document describes the backend endpoints, payloads, and events required to
support the tab bar use cases and related admin settings in the app.

## Environment

- `EXPO_PUBLIC_API_BASE_URL`: Base API URL used by the app. Example: `https://api.example.com/api/v1`
- `EXPO_PUBLIC_EVENTS_URL` or `EXPO_PUBLIC_WS_URL`: Optional WebSocket URL for backend events.

All authenticated requests use `Authorization: Bearer <accessToken>`.

## Authentication Endpoints

### POST /public/auth/login

Request:
```json
{
  "provider": "MANUAL",
  "email": "user@example.com",
  "password": "secret",
  "device": {
    "deviceId": "abc-123",
    "deviceName": "Pixel 7",
    "platform": "ANDROID"
  }
}
```

Response:
```json
{
  "tokens": {
    "accessToken": "jwt",
    "expiresInSeconds": 3600,
    "refreshToken": "jwt",
    "refreshExpiresInSeconds": 1209600
  },
  "user": {
    "id": "user_1",
    "email": "user@example.com",
    "role": "USER",
    "provider": "MANUAL",
    "name": "Rory"
  }
}
```

### POST /public/auth/refresh

Request:
```json
{
  "refreshToken": "jwt",
  "deviceId": "abc-123"
}
```

Response: same shape as login.

Role values: `GUEST`, `USER`, `ADMIN`.

Auth responses return a user summary (no `username`). Use `/users/me` to
hydrate profile fields.

### POST /public/auth/logout

Auth required. Returns `204`.

### POST /public/auth/admin-passcode

Request:
```json
{
  "passcode": "123456",
  "device": {
    "deviceId": "abc-123",
    "deviceName": "Pixel 7",
    "platform": "ANDROID"
  }
}
```

Response: same shape as login.

### POST /public/auth/social

Request:
```json
{
  "provider": "GOOGLE",
  "token": "oauth-token",
  "device": {
    "deviceId": "abc-123",
    "deviceName": "Pixel 7",
    "platform": "ANDROID"
  }
}
```

Response: same shape as login.

## User Profile

### GET /users/me

Auth required. Response:
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

### PUT /users/me

Auth required. Request (any subset):
```json
{
  "name": "Rory",
  "username": "rory",
  "avatarUrl": "https://cdn.example.com/avatar.png"
}
```

Response: same shape as `GET /users/me`.

## App Configuration Endpoints

### GET /public/app-config

Response:
```json
{
  "socialLoginEnabled": true,
  "postsPerPage": 10,
  "splash": {
    "enabled": false,
    "mediaType": "image",
    "mediaUrl": "",
    "duration": 5000,
    "showCloseButton": true,
    "closeButtonDelay": 2000
  }
}
```

### PUT /admin/app-config

Auth required. Request and response shape is the same as `/public/app-config`.

## Navigation Configuration (Tab Bar)

### GET /public/navigation-config

Response:
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

### PUT /admin/navigation-config

Auth required. Request and response shape matches `/public/navigation-config`.

Server rules:
- Always keep `settings` and `index` enabled.
- Preserve `order` as the source of truth for tab ordering.

## Admin Settings Configuration

### GET /public/admin-settings

Response:
```json
{
  "authMethods": { "google": true, "apple": true, "manual": true },
  "authServiceModes": { "google": "mock", "apple": "mock", "manual": "mock" },
  "sessionConfig": { "maxTimeMinutes": 30, "idleTimeMinutes": 15, "autoRefresh": true },
  "languageConfig": { "availableCodes": ["en", "es"], "defaultCode": "en" },
  "profileConfig": {
    "usernameMinLength": 3,
    "usernameMaxLength": 30,
    "avatarMaxSizeMB": 5,
    "allowedAvatarFormats": ["image/png", "image/jpeg", "image/jpg", "image/svg+xml"]
  }
}
```

### PUT /admin/admin-settings

Auth required. Request and response shape matches `/public/admin-settings`.

Server rules:
- At least one auth method enabled.
- At least one language enabled.
- `defaultCode` must be within `availableCodes`.
- `usernameMaxLength` must be > `usernameMinLength`.

## User Management

### GET /admin/users

Auth required. Response:
```json
[
  {
    "id": "user_1",
    "name": "Rory",
    "username": "rory",
    "email": "user@example.com",
    "role": "USER",
    "status": "ACTIVE",
    "provider": "GOOGLE",
    "createdAt": "2025-10-10T12:00:00Z",
    "lastLoginAt": "2025-10-12T09:00:00Z"
  }
]
```

### PATCH /admin/users/{id}

Auth required. Request (any subset):
```json
{
  "name": "New Name",
  "username": "newname",
  "role": "ADMIN",
  "status": "DISABLED"
}
```

Response: updated user object (same shape as GET).

### DELETE /admin/users/{id}

Auth required. Returns `204`.

Server rules:
- Prevent deleting or disabling the currently authenticated admin account.

## Backend Events (Optional)

If WebSocket events are enabled, send events when config changes:

- `type: "admin-settings.updated"`
- `type: "navigation.updated"`
- `type: "app-config.updated"`

The client listens for any event containing `settings`, `auth`, `language`, `profile`, or `session`
in the type string to refresh admin settings.

## Error Handling

Use standard HTTP status codes:

- `400` validation error (include details in response body)
- `401` unauthorized
- `403` forbidden
- `404` not found
- `409` conflict (duplicate, invalid state)
- `500` server error

All error responses should be JSON with a message field.
