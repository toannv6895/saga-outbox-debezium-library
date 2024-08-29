CREATE TABLE IF NOT EXISTS outboxevent
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    timestamp     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id   VARCHAR(100) NOT NULL,
    type          VARCHAR(100) NOT NULL,
    payload       JSON        NOT NULL
);

ALTER TABLE outboxevent
    REPLICA IDENTITY FULL;
