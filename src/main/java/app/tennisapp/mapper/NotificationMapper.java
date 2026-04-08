package app.tennisapp.mapper;

import app.tennisapp.dto.NotificationDto;
import app.tennisapp.entity.Notification;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationMapper {
    public NotificationDto toDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getMessage(),
                notification.getType(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }

    public List<NotificationDto> toDto(List<Notification> notifications) {
        return notifications.stream()
                .map(this::toDto)
                .toList();
    }
}