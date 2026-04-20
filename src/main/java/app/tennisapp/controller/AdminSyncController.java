package app.tennisapp.controller;

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

    private final SyncService syncService;

    @PostMapping("/tournaments")
    public ResponseEntity<Void> syncTournaments() {
        syncService.syncTournaments();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/standings")
    public ResponseEntity<Void> syncStandings() {
        syncService.syncStandings();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/fixtures")
    public ResponseEntity<Void> syncFixtures(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        syncService.syncFixtures(start, end);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/livescores")
    public ResponseEntity<Void> syncLivescores() {
        syncService.syncLivescores();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/player/{externalId}")
    public ResponseEntity<Void> syncPlayer(@PathVariable Long externalId) {
        syncService.syncPlayer(externalId);
        return ResponseEntity.noContent().build();
    }
}