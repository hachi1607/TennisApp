CREATE TABLE matches
(
    id               BIGSERIAL PRIMARY KEY,
    external_id      BIGINT  NOT NULL UNIQUE,
    first_player_id  BIGINT  NOT NULL REFERENCES players (id),
    second_player_id BIGINT  NOT NULL REFERENCES players (id),
    tournament_id    BIGINT  NOT NULL REFERENCES tournaments (id),
    season           VARCHAR(10),
    date             DATE    NOT NULL,
    time             VARCHAR(10),
    final_result     VARCHAR(50),
    game_result      VARCHAR(50),
    event_serve      VARCHAR(50),
    winner           VARCHAR(50),
    status           VARCHAR(50),
    round            VARCHAR(100),
    is_live          BOOLEAN NOT NULL DEFAULT FALSE,
    qualification    BOOLEAN NOT NULL DEFAULT FALSE,
    scores_json      jsonb,
    last_synced_at   TIMESTAMP
);