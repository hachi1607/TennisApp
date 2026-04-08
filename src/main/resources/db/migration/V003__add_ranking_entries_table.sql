CREATE TABLE ranking_entries
(
    id           BIGSERIAL PRIMARY KEY,
    player_id    BIGINT      NOT NULL REFERENCES players (id) ON DELETE CASCADE,
    external_id  BIGINT,
    position     INT         NOT NULL,
    points       INT         NOT NULL,
    movement     VARCHAR(20),
    ranking_type VARCHAR(10) NOT NULL,
    ranking_date DATE        NOT NULL
);