package app.tennisapp.service;

import app.tennisapp.entity.SyncEntityType;
import app.tennisapp.entity.SyncLog;
import app.tennisapp.entity.SyncStatus;
import app.tennisapp.mapper.SyncLogMapper;
import app.tennisapp.repository.SyncLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SyncLogService {
    private final SyncLogRepository syncLogRepository;
    private final SyncLogMapper syncLogMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSync(SyncEntityType entityType, SyncStatus status, String message) {
        syncLogRepository.save(syncLogMapper.toEntity(entityType, status, message));
    }
}
