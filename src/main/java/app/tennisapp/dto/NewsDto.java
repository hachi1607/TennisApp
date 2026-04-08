package app.tennisapp.dto;

import java.time.LocalDateTime;

public record NewsDto(
        Long id,
        Long authorId,
        String authorEmail,
        String title,
        String content,
        String imageUrl,
        LocalDateTime publishedAt
) {
}