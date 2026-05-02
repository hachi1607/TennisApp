package app.tennisapp.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record MatchFilterParams(
        Boolean live,
        Long playerId,
        Long firstPlayerId,
        Long secondPlayerId,
        Long tournamentId,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
) {}
