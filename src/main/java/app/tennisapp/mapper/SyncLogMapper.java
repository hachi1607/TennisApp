package app.tennisapp.mapper;

import app.tennisapp.entity.SyncEntityType;
import app.tennisapp.entity.SyncLog;
import app.tennisapp.entity.SyncStatus;
import org.springframework.stereotype.Component;

@Component
public class SyncLogMapper {
    public SyncLog toEntity(SyncEntityType entityType, SyncStatus status, String errorMessage) {
        return SyncLog.builder()
                .entityType(entityType)
                .status(status)
                .errorMessage(status == SyncStatus.FAILED ? errorMessage : null)
                .build();
    }
}