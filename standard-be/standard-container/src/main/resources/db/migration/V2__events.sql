CREATE TABLE events (
                         id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         title        VARCHAR(200) NOT NULL,
                         slug         VARCHAR(200) NOT NULL UNIQUE,
                         excerpt      VARCHAR(500),
                         tags         TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
                         status       VARCHAR(16) NOT NULL,
                         thumbnail    JSONB,
                         content      JSONB NOT NULL,
                         start_at     TIMESTAMPTZ,
                         end_at       TIMESTAMPTZ,
                         timezone     VARCHAR(64),
                         location     VARCHAR(255),
                         tickets_url  VARCHAR(500),
                         created_by   UUID REFERENCES users(id),
                         created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
                         updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_events_status_start_at
    ON events(status, start_at DESC);

CREATE INDEX idx_events_status_updated_at
    ON events(status, updated_at DESC);
