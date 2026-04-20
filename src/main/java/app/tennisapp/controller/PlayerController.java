package app.tennisapp.controller;

import app.tennisapp.dto.PlayerDto;
import app.tennisapp.dto.PlayerSeasonStatsDto;
import app.tennisapp.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/player")
@RestController
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping
    public ResponseEntity<List<PlayerDto>> getAllPlayers() {
        return ResponseEntity.ok().body(playerService.getAllPlayers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerDto> getPlayerById(@PathVariable Long id) {
        return ResponseEntity.ok().body(playerService.getPlayerById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PlayerDto>> searchPlayersByName(@RequestParam String name) {
        return ResponseEntity.ok().body(playerService.searchPlayersByName(name));
    }

    @GetMapping("/nationality/{nationality}")
    public ResponseEntity<List<PlayerDto>> getPlayersByNationality(@PathVariable String nationality) {
        return ResponseEntity.ok().body(playerService.getPlayersByNationality(nationality));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<List<PlayerSeasonStatsDto>> getPlayerStats(@PathVariable Long id) {
        return ResponseEntity.ok().body(playerService.getPlayerStats(id));
    }
}