# Standard BE

Standard BE is a modular Spring Boot backend organized with DDD + hexagonal layering.
The domain core is framework-free, application services define use cases and ports,
adapters implement delivery/data access, and a container module wires everything.

## Use cases
- Public auth: register, login, refresh, logout.
- Public feed: list published posts, fetch post details by slug.
- Admin posts: create drafts, update content, publish posts.

## Functionality
- Clean/hexagonal modules with strict dependency direction.
- JWT-like access tokens and rotating refresh tokens.
- Feed and admin post orchestration via application services.
- Flyway migrations and PostgreSQL persistence.
- OpenAPI-generated interfaces and models, grouped by tags.

## API
Base path: `/api/v1`

Public auth
- `POST /public/auth/register`
- `POST /public/auth/login`
- `POST /public/auth/refresh`
- `POST /public/auth/logout`

Public feed
- `GET /public/feed`
- `GET /public/posts/{slug}`

Admin posts (requires `ROLE_ADMIN`)
- `POST /admin/posts`
- `PUT /admin/posts/{id}`
- `POST /admin/posts/{id}/publish`

OpenAPI docs
- `GET /swagger-ui/index.html`
- `GET /v3/api-docs`

## Run locally
1) Start Postgres

```
docker-compose up -d
```

2) Run tests

```
mvn test
```

3) Start the container app

```
mvn -pl standard-container spring-boot:run
```

## Observability (optional)
Start Elasticsearch + Logstash + Kibana for local/dev.

```
docker compose --profile local up -d elasticsearch logstash kibana
```

If the app runs on the host, keep `LOGSTASH_HOST=localhost`. If you run it in a container on the same compose network, set `LOGSTASH_HOST=logstash`.

Prometheus metrics are exposed at `/actuator/prometheus`.
