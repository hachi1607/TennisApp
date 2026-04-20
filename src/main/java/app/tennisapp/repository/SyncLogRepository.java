package app.tennisapp.repository;

import app.tennisapp.entity.SyncEntityType;
import app.tennisapp.entity.SyncLog;
import app.tennisapp.entity.SyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SyncLogRepository extends JpaRepository<SyncLog, Long> {
    List<SyncLog> findByEntityTypeOrderBySyncedAtDesc(SyncEntityType entityType);

    Optional<SyncLog> findTopByEntityTypeOrderBySyncedAtDesc(SyncEntityType entityType);

    List<SyncLog> findByStatus(SyncStatus status);
}