package app.tennisapp.service;

import app.tennisapp.entity.SyncEntityType;
import app.tennisapp.entity.SyncLog;
import app.tennisapp.entity.SyncStatus;
import app.tennisapp.mapper.SyncLogMapper;
import app.tennisapp.repository.SyncLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncLogServiceTest {
    @Mock
    private SyncLogRepository syncLogRepository;
    @Mock
    private SyncLogMapper syncLogMapper;
    @InjectMocks
    private SyncLogService syncLogService;

    private SyncLog buildSyncLog(SyncEntityType entityType, SyncStatus status, String errorMessage) {
        return SyncLog.builder()
                .entityType(entityType)
                .status(status)
                .errorMessage(errorMessage)
                .build();
    }

    @Test
    void shouldSaveSuccessLog() {
        SyncLog log = buildSyncLog(SyncEntityType.TOURNAMENT, SyncStatus.SUCCESS, null);

        when(syncLogMapper.toEntity(SyncEntityType.TOURNAMENT, SyncStatus.SUCCESS, null))
                .thenReturn(log);

        syncLogService.logSync(SyncEntityType.TOURNAMENT, SyncStatus.SUCCESS, null);

        verify(syncLogMapper, times(1)).toEntity(SyncEntityType.TOURNAMENT, SyncStatus.SUCCESS, null);
        verify(syncLogRepository, times(1)).save(log);
    }

    @Test
    void shouldSaveFailedLog() {
        SyncLog log = buildSyncLog(SyncEntityType.MATCH, SyncStatus.FAILED, "Connection timeout");

        when(syncLogMapper.toEntity(SyncEntityType.MATCH, SyncStatus.FAILED, "Connection timeout"))
                .thenReturn(log);

        syncLogService.logSync(SyncEntityType.MATCH, SyncStatus.FAILED, "Connection timeout");

        verify(syncLogMapper, times(1)).toEntity(SyncEntityType.MATCH, SyncStatus.FAILED, "Connection timeout");
        verify(syncLogRepository, times(1)).save(log);
    }
}
