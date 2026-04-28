package app.tennisapp.service;

import app.tennisapp.repository.SyncLogRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SyncLogServiceTest {
    @Mock
    private SyncLogRepository syncLogRepository;
    @InjectMocks
    private SyncLogService syncLogService;
}