CREATE TABLE players
(
    id             BIGSERIAL PRIMARY KEY,
    external_id    BIGINT       NOT NULL UNIQUE,
    full_name      VARCHAR(255) NOT NULL,
    nationality    VARCHAR(100),
    birth_date     DATE,
    bio            TEXT,
    image_url      VARCHAR(500),
    last_synced_at TIMESTAMP
);

CREATE TABLE player_season_stats
(
    id           BIGSERIAL PRIMARY KEY,
    player_id    BIGINT      NOT NULL REFERENCES players (id) ON DELETE CASCADE,
    season       VARCHAR(10) NOT NULL,
    type         VARCHAR(20) NOT NULL,
    rank         INT DEFAULT 0,
    titles       INT DEFAULT 0,
    matches_won  INT DEFAULT 0,
    matches_lost INT DEFAULT 0,
    hard_won     INT DEFAULT 0,
    hard_lost    INT DEFAULT 0,
    clay_won     INT DEFAULT 0,
    clay_lost    INT DEFAULT 0,
    grass_won    INT DEFAULT 0,
    grass_lost   INT DEFAULT 0,
    UNIQUE (player_id, season, type)
);