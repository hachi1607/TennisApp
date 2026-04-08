CREATE TABLE tournaments
(
    id             BIGSERIAL PRIMARY KEY,
    external_id    BIGINT       NOT NULL UNIQUE,
    name           VARCHAR(255) NOT NULL,
    event_category VARCHAR(20)  NOT NULL
);