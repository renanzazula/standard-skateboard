-- Enable UUID generation (pick one extension)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- USERS
CREATE TABLE users (
                       id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       email        VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255),
                       role         VARCHAR(16) NOT NULL,
                       provider     VARCHAR(16) NOT NULL,
                       status       VARCHAR(16) NOT NULL,
                       created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
                       updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- REFRESH TOKENS (rotating)
CREATE TABLE refresh_tokens (
                                id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                user_id             UUID NOT NULL REFERENCES users(id),
                                token_hash          VARCHAR(64) NOT NULL UNIQUE,
                                token_family_id     UUID NOT NULL,
                                device_id           VARCHAR(128) NOT NULL,
                                device_name         VARCHAR(128),
                                issued_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
                                expires_at          TIMESTAMPTZ NOT NULL,
                                last_used_at        TIMESTAMPTZ,
                                revoked_at          TIMESTAMPTZ,
                                replaced_by_token_id UUID,
                                CONSTRAINT fk_replaced_by
                                    FOREIGN KEY (replaced_by_token_id) REFERENCES refresh_tokens(id)
);

CREATE INDEX idx_refresh_user_active
    ON refresh_tokens(user_id)
    WHERE revoked_at IS NULL;

CREATE INDEX idx_refresh_family_active
    ON refresh_tokens(token_family_id)
    WHERE revoked_at IS NULL;

-- POSTS
CREATE TABLE posts (
                       id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       title        VARCHAR(200) NOT NULL,
                       slug         VARCHAR(200) NOT NULL UNIQUE,
                       excerpt      VARCHAR(500),
                       tags         TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
                       status       VARCHAR(16) NOT NULL,
                       thumbnail    JSONB,
                       content      JSONB NOT NULL,
                       author_id    UUID REFERENCES users(id),
                       created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
                       updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
                       published_at TIMESTAMPTZ
);

CREATE INDEX idx_posts_status_published_at
    ON posts(status, published_at DESC);
