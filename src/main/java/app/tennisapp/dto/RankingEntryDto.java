package app.tennisapp.dto;

import app.tennisapp.entity.RankingType;

import java.time.LocalDate;

public record RankingEntryDto(
        Long id,
        Long playerId,
        String playerName,
        String nationality,
        int position,
        int points,
        String movement,
        RankingType rankingType,
        LocalDate rankingDate
) {
}