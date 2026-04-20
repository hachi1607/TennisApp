package app.tennisapp.service;

import app.tennisapp.entity.SyncEntityType;
import app.tennisapp.entity.SyncLog;
import app.tennisapp.entity.SyncStatus;
import app.tennisapp.repository.SyncLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SyncLogService {
    private final SyncLogRepository syncLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW) // Spring suspends the current transaction if it exists, and then creates a new one
    public void logSync(SyncEntityType entityType, SyncStatus status, String message) {
        SyncLog entry = SyncLog.builder()
                .entityType(entityType)
                .status(status)
                .errorMessage(status == SyncStatus.FAILED ? message : null)
                .build();
        syncLogRepository.save(entry);
    }
}
