package app.tennisapp.dto;

import app.tennisapp.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationDto(
        Long id,
        String message,
        NotificationType type,
        boolean isRead,
        LocalDateTime createdAt
) {
}