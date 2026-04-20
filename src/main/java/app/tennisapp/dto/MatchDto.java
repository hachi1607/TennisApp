package app.tennisapp.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;

public record MatchDto(
        Long id,
        Long externalId,
        Long firstPlayerId,
        String firstPlayerName,
        Long secondPlayerId,
        String secondPlayerName,
        Long tournamentId,
        String tournamentName,
        String season,
        LocalDate date,
        String time,
        String finalResult,
        String gameResult,
        String eventServe,
        String winner,
        String status,
        String round,
        boolean isLive,
        boolean qualification,
        JsonNode scoresJson
) {
}