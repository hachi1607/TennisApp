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

    @Scheduled(cron = "0 0 3 * * *")
    public void syncTournamentsDaily() {
        try {
            log.info("Starting scheduled tournaments sync");
            syncService.syncTournaments();
        } catch (Exception e) {
            log.error("Failed to sync tournaments", e);
            syncLogService.logSync(SyncEntityType.TOURNAMENT, SyncStatus.FAILED, e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 5 * * MON")
    public void syncStandingsWeekly() {
        try {
            log.info("Starting scheduled standings sync");
            syncService.syncStandings();
        } catch (Exception e) {
//            log.error("Failed to sync player externalId=" + externalPlayerKey, e); //TODO poprawić
            syncLogService.logSync(SyncEntityType.PLAYER, SyncStatus.FAILED, e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 4 * * *")
    public void syncFixturesDaily() {
        log.info("Starting scheduled fixtures sync");
        LocalDate today = LocalDate.now();
        syncService.syncFixtures(today.minusDays(1), today.plusDays(7));
    }

    @Scheduled(fixedDelay = 120_000, initialDelay = 60_000)
    public void syncLivescoresFrequently() {
        log.debug("Starting scheduled livescores sync");
        syncService.syncLivescores();
    }
}