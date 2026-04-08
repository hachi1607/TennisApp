package app.tennisapp.dto;

public record PlayerSeasonStatsDto(
        Long id,
        Long playerId,
        String season,
        String type,
        int rank,
        int titles,
        int matchesWon,
        int matchesLost,
        int hardWon,
        int hardLost,
        int clayWon,
        int clayLost,
        int grassWon,
        int grassLost
) {
}