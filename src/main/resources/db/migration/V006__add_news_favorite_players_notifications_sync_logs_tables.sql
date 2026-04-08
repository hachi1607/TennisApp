CREATE TABLE news
(
    id           BIGSERIAL PRIMARY KEY,
    author_id    BIGINT       NOT NULL REFERENCES users (id),
    title        VARCHAR(255) NOT NULL,
    content      TEXT         NOT NULL,
    image_url    VARCHAR(500),
    published_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE favorite_players
(
    user_id   BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    player_id BIGINT    NOT NULL REFERENCES players (id) ON DELETE CASCADE,
    added_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, player_id)
);

CREATE TABLE notifications
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    message    VARCHAR(500) NOT NULL,
    type       VARCHAR(50)  NOT NULL,
    is_read    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE sync_logs
(
    id            BIGSERIAL PRIMARY KEY,
    entity_type   VARCHAR(100) NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    synced_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    error_message TEXT
);