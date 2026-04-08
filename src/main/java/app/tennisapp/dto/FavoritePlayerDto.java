package app.tennisapp.dto;

import java.time.LocalDateTime;

public record FavoritePlayerDto(
        Long userId,
        Long playerId,
        String playerFullName,
        String playerImageUrl,
        LocalDateTime addedAt
) {
}