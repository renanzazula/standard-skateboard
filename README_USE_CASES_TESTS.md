# Standard Skateboard Backend - Use Cases and Tests

This document summarizes backend use cases and the current automated tests,
including integration coverage.

## Use cases

Public auth
- Register user: `POST /public/auth/register`
- Login user: `POST /public/auth/login`
- Admin passcode login: `POST /public/auth/admin-passcode`
- Social login: `POST /public/auth/social`
- Refresh token: `POST /public/auth/refresh`
- Logout: `POST /public/auth/logout`

Public feed (posts + events)
- List published feed items (ETag/Last-Modified): `GET /public/feed`
- Fetch post by slug: `GET /public/posts/{slug}`

Admin posts (requires `ROLE_ADMIN`)
- Create draft: `POST /admin/posts`
- Update post: `PUT /admin/posts/{id}`
- Publish post: `POST /admin/posts/{id}/publish`
- Fetch post: `GET /admin/posts/{id}`
- Delete post: `DELETE /admin/posts/{id}`
- Import posts: `POST /admin/posts/import`
- Reset posts: `POST /admin/posts/reset`

Public events
- List published events (ETag/Last-Modified): `GET /public/events`
- Fetch event by slug: `GET /public/events/{slug}`

Admin events (requires `ROLE_ADMIN`)
- Create draft: `POST /admin/events`
- Update event: `PUT /admin/events/{id}`
- Publish event: `POST /admin/events/{id}/publish`
- Fetch event: `GET /admin/events/{id}`
- Delete event: `DELETE /admin/events/{id}`
- Import events: `POST /admin/events/import`
- Reset events: `POST /admin/events/reset`

App config
- Public config: `GET /public/app-config`
- Admin config: `GET /admin/app-config`, `PUT /admin/app-config`

Navigation config
- Public config: `GET /public/navigation-config`
- Admin config: `PUT /admin/navigation-config`

WebSocket events
- Feed updates: `WS /ws/feed` (types containing `feed` or `post`)
- Event updates: `WS /ws/events` (types containing `event` or `events`)
- Navigation updates: `WS /ws/events` (type `navigation.updated`)

Observability
- Prometheus metrics: `GET /actuator/prometheus`

## Test cases

Domain/value objects
- `standard-be/common/common-domain/src/test/java/com/skateboard/podcast/domain/valueobject/PostStatusTest.java`
- `standard-be/common/common-domain/src/test/java/com/skateboard/podcast/domain/valueobject/TagTest.java`

Feed REST adapters
- `standard-be/feed-service/feed-application/src/test/java/com/skateboard/podcast/feed/service/application/adapter/in/rest/AdminPostsControllerTest.java`
- `standard-be/feed-service/feed-application/src/test/java/com/skateboard/podcast/feed/service/application/adapter/in/rest/PublicFeedControllerTest.java`

Feed application services
- `standard-be/feed-service/feed-domain/feed-application-service/src/test/java/com/skateboard/podcast/feed/service/application/dto/FeedVersionTest.java`
- `standard-be/feed-service/feed-domain/feed-application-service/src/test/java/com/skateboard/podcast/feed/service/application/service/AdminPostsServiceTest.java`
- `standard-be/feed-service/feed-domain/feed-application-service/src/test/java/com/skateboard/podcast/feed/service/application/service/PublicFeedServiceTest.java`
- `standard-be/feed-service/feed-domain/feed-application-service/src/test/java/com/skateboard/podcast/feed/service/application/service/PublicPostsServiceTest.java`

Feed data access
- `standard-be/feed-service/feed-dataaccess/src/test/java/com/skateboard/podcast/feed/service/dataaccess/persistence/PostRepositoryAdapterTest.java`

IAM REST adapter
- `standard-be/iam-service/iam-application/src/test/java/com/skateboard/podcast/iam/service/application/adapter/in/rest/PublicAuthControllerTest.java`

IAM application services
- `standard-be/iam-service/iam-domain/iam-application-service/src/test/java/com/skateboard/podcast/iam/service/application/service/LoginServiceTest.java`
- `standard-be/iam-service/iam-domain/iam-application-service/src/test/java/com/skateboard/podcast/iam/service/application/service/LogoutServiceTest.java`
- `standard-be/iam-service/iam-domain/iam-application-service/src/test/java/com/skateboard/podcast/iam/service/application/service/RefreshServiceTest.java`
- `standard-be/iam-service/iam-domain/iam-application-service/src/test/java/com/skateboard/podcast/iam/service/application/service/RegisterServiceTest.java`

IAM data access/crypto
- `standard-be/iam-service/iam-dataaccess/src/test/java/com/skateboard/podcast/iam/service/dataaccess/crypto/TokenServiceTest.java`
- `standard-be/iam-service/iam-dataaccess/src/test/java/com/skateboard/podcast/iam/service/dataaccess/persistence/RefreshTokenRepositoryAdapterTest.java`
- `standard-be/iam-service/iam-dataaccess/src/test/java/com/skateboard/podcast/iam/service/dataaccess/persistence/UserRepositoryAdapterTest.java`

WebSocket publisher
- `standard-be/standard-container/src/test/java/com/skateboard/podcast/standard/service/container/websocket/WebSocketFeedEventPublisherTest.java`

## Integration tests

Container boot and wiring (Testcontainers + Spring Boot)
- `standard-be/standard-container/src/test/java/com/skateboard/podcast/standard/service/container/StandardContainerApplicationTest.java`

Events REST + WebSocket flow (Testcontainers + Spring Boot + WS)
- `standard-be/standard-container/src/test/java/com/skateboard/podcast/standard/service/container/events/EventsIntegrationTest.java`

Navigation config REST + WebSocket flow (Testcontainers + Spring Boot + WS)
- `standard-be/standard-container/src/test/java/com/skateboard/podcast/standard/service/container/navigationconfig/NavigationConfigIntegrationTest.java`

## How to run tests

All tests (from `standard-be`):
```
mvn test
```

Standard container module only:
```
mvn -pl standard-container -am test
```

Notes:
- Integration tests use Testcontainers and require Docker.
- The events integration test opens a local WebSocket to verify `/ws/events`.
