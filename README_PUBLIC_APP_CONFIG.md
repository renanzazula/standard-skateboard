d# Public App Config Endpoint

This README describes how to implement `GET /public/app-config`, which supplies
app-level configuration to the mobile/web client.

## Overview

The client fetches this config on startup and stores it locally. It is used to:

- toggle social login availability
- control the feed page size
- drive splash screen behavior (media + timing)

Client references:

- `lib/api.ts` -> `publicAppConfigGet`
- `contexts/AppConfigContext.tsx` -> caching + refresh
- `contexts/SplashContext.tsx` and `components/SplashScreen.tsx`

## Endpoint

```
GET /public/app-config
```

- Auth: none
- Content-Type: `application/json`

## Response schema

`200 OK` with an `AppConfig` payload:

```json
{
  "socialLoginEnabled": true,
  "postsPerPage": 10,
  "splash": {
    "enabled": false,
    "mediaType": "image",
    "mediaUrl": null,
    "duration": 5000,
    "showCloseButton": true,
    "closeButtonDelay": 2000
  }
}
```

### AppConfig fields

- `socialLoginEnabled` (boolean): show/hide social login in the UI.
- `postsPerPage` (integer): page size used by the feed client.
  - Constraints: min 5, max 50.
- `splash` (object): splash screen configuration.

### SplashConfig fields

- `enabled` (boolean): when false, splash is skipped entirely.
- `mediaType` ("image" | "video"): media type for the splash screen.
- `mediaUrl` (string | null): absolute URL for the media asset.
  - Empty string or null means "no media" and splash will not show.
- `duration` (integer, ms): auto-close timer for the splash screen.
  - Used directly by the client in milliseconds.
- `showCloseButton` (boolean): whether to show the close button.
- `closeButtonDelay` (integer, ms): delay before the close button appears.

## Validation rules

- Always return all required fields.
- Enforce `postsPerPage` range [5, 50].
- `duration` and `closeButtonDelay` must be >= 0 (milliseconds).
- `mediaType` must be `image` or `video`.

## Defaults (client-side)

If the client receives partial data or fails to fetch, it normalizes to:

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

## Optional realtime refresh

The client listens for backend events (WebSocket). Any event type containing
`config`, `settings`, `splash`, or `auth` triggers a refresh. Recommended event:

```json
{ "type": "app-config.updated", "timestamp": "2026-02-07T12:34:56.000Z" }
```

## Related docs

- `docs/openapi.yaml` (schemas `AppConfig`, `SplashConfig`)
- `docs/BE_INTEGRATION.md` (app-config examples)
