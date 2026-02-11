CREATE TABLE settings_config (
    id          BIGINT PRIMARY KEY,
    config_json TEXT NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO settings_config (id, config_json)
VALUES (
    1,
    '{"enabledAuthMethods":{"google":false,"apple":false,"manual":true},"serviceModes":{"google":"mock","apple":"mock","manual":"mock"},"sessionConfig":{"maxTime":1800000,"idleTime":900000,"autoRefresh":true},"languageConfig":{"availableLanguages":[{"code":"en","name":"English","nativeName":"English","flag":"EN"},{"code":"es","name":"Spanish","nativeName":"Espanol","flag":"ES"}],"defaultLanguage":{"code":"en","name":"English","nativeName":"English","flag":"EN"}},"profileConfig":{"usernameMinLength":3,"usernameMaxLength":30,"avatarMaxSizeMB":5,"allowedAvatarFormats":["image/png","image/jpeg","image/jpg","image/svg+xml"]},"feedRealtimeEnabled":true}'
)
ON CONFLICT (id) DO NOTHING;
