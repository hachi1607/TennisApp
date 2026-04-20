package app.tennisapp.controller;

import app.tennisapp.entity.SyncEntityType;
import app.tennisapp.entity.SyncStatus;
import app.tennisapp.service.SyncLogService;
import app.tennisapp.service.SyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RequiredArgsConstructor
@RequestMapping("/admin/sync")
@RestController
public class AdminSyncController {
    private final SyncLogService syncLogService;
    private final SyncService syncService;

    @PostMapping("/tournaments")
    public ResponseEntity<Void> syncTournaments() {
        syncService.syncTournaments();
        syncLogService.logSync(SyncEntityType.TOURNAMENT, SyncStatus.SUCCESS, null);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/standings")
    public ResponseEntity<Void> syncStandings() {
        syncService.syncStandings();
        syncLogService.logSync(SyncEntityType.RANKING, SyncStatus.SUCCESS, null);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/fixtures")
    public ResponseEntity<Void> syncFixtures(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        syncService.syncFixtures(start, end);
        syncLogService.logSync(SyncEntityType.MATCH, SyncStatus.SUCCESS, null);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/livescores")
    public ResponseEntity<Void> syncLivescores() {
        syncService.syncLivescores();
        syncLogService.logSync(SyncEntityType.MATCH, SyncStatus.SUCCESS, null);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/player/{externalId}")
    public ResponseEntity<Void> syncPlayer(@PathVariable Long externalId) {
        syncService.syncPlayer(externalId);
        syncLogService.logSync(SyncEntityType.PLAYER, SyncStatus.SUCCESS, null);
        return ResponseEntity.noContent().build();
    }
}