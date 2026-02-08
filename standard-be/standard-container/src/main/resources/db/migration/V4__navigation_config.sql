CREATE TABLE navigation_config (
    id          BIGINT PRIMARY KEY,
    config_json TEXT NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO navigation_config (id, config_json)
VALUES (
    1,
    '{"tabs":[{"id":"index","name":"Events","icon":"Calendar","order":0,"enabled":true,"isSystem":true},{"id":"settings","name":"Settings","icon":"Settings","order":1,"enabled":true,"isSystem":true}]}'
)
ON CONFLICT (id) DO NOTHING;
