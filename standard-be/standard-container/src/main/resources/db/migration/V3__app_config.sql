CREATE TABLE app_config (
    id                         BIGINT PRIMARY KEY,
    social_login_enabled       BOOLEAN NOT NULL,
    posts_per_page             INTEGER NOT NULL,
    splash_enabled             BOOLEAN NOT NULL,
    splash_media_type          VARCHAR(20) NOT NULL,
    splash_media_url           VARCHAR(500),
    splash_duration            INTEGER NOT NULL,
    splash_show_close_button   BOOLEAN NOT NULL,
    splash_close_button_delay  INTEGER NOT NULL,
    updated_at                 TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO app_config (
    id,
    social_login_enabled,
    posts_per_page,
    splash_enabled,
    splash_media_type,
    splash_media_url,
    splash_duration,
    splash_show_close_button,
    splash_close_button_delay
)
VALUES (
    1,
    FALSE,
    20,
    FALSE,
    'image',
    NULL,
    0,
    TRUE,
    0
)
ON CONFLICT (id) DO NOTHING;
