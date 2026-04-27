package app.tennisapp.scheduler;

import app.tennisapp.entity.SyncEntityType;
import app.tennisapp.entity.SyncStatus;
import app.tennisapp.service.SyncLogService;
import app.tennisapp.service.SyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class TennisSyncScheduler {
    private final SyncService syncService;
    private final SyncLogService syncLogService;

    @Scheduled(cron = "${scheduler.sync.tournaments-cron}")
    public void syncTournamentsDaily() {
        log.info("Starting scheduled tournaments sync");
        try {
            syncService.syncTournaments();
            syncLogService.logSync(SyncEntityType.TOURNAMENT, SyncStatus.SUCCESS, null);
        } catch (Exception e) {
            log.error("Scheduled tournaments sync failed", e);
            syncLogService.logSync(SyncEntityType.TOURNAMENT, SyncStatus.FAILED, e.getMessage());
        }
    }

    @Scheduled(cron = "${scheduler.sync.standings-cron}")
    public void syncStandingsWeekly() {
        log.info("Starting scheduled standings sync");
        try {
            syncService.syncStandings();
            syncLogService.logSync(SyncEntityType.RANKING, SyncStatus.SUCCESS, null);
        } catch (Exception e) {
            log.error("Scheduled standings sync failed", e); //TODO poprawić
            syncLogService.logSync(SyncEntityType.RANKING, SyncStatus.FAILED, e.getMessage());
        }
    }

    @Scheduled(cron = "${scheduler.sync.fixtures-cron}")
    public void syncFixturesDaily() {
        log.info("Starting scheduled fixtures sync");
        try {
            LocalDate today = LocalDate.now();
            syncService.syncFixtures(today.minusDays(1), today.plusDays(7));
            syncLogService.logSync(SyncEntityType.MATCH, SyncStatus.SUCCESS, null);
        } catch (Exception e) {
            log.error("Scheduled fixtures sync failed", e); //TODO poprawić
            syncLogService.logSync(SyncEntityType.MATCH, SyncStatus.FAILED, e.getMessage());
        }
    }

    @Scheduled(fixedDelayString = "${scheduler.sync.livescores-delay-ms}",
            initialDelayString = "${scheduler.sync.livescores-initial-delay-ms}")
    public void syncLivescoresFrequently() {
        log.debug("Starting scheduled livescores sync");
        try {
            syncService.syncLivescores();
            syncLogService.logSync(SyncEntityType.MATCH, SyncStatus.SUCCESS, null);
        } catch (Exception e) {
            log.error("Scheduled fixtures sync failed", e); //TODO poprawić
            syncLogService.logSync(SyncEntityType.MATCH, SyncStatus.FAILED, e.getMessage());
        }
    }
}