CREATE TABLE IF NOT EXISTS outboxevent (
    id             BINARY(16) NOT NULL PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    timestamp      TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id   VARCHAR(100) NOT NULL,
    type           VARCHAR(100) NOT NULL,
    payload        JSON        NOT NULL
);

CREATE TABLE IF NOT EXISTS consumedmessage
(
    event_id  BINARY(16) NOT NULL PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    time_of_receiving TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
