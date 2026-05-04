package app.tennisapp.controller;

import app.tennisapp.dto.PlayerDto;
import app.tennisapp.dto.PlayerSeasonStatsDto;
import app.tennisapp.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/players")
@RestController
public class PlayerController {
    private final PlayerService playerService;

    @GetMapping
    public ResponseEntity<Page<PlayerDto>> getPlayers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String nationality,
            @PageableDefault(sort = "fullName", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok().body(playerService.getPlayers(name, nationality, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerDto> getPlayerById(@PathVariable Long id) {
        return ResponseEntity.ok().body(playerService.getPlayerById(id));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<List<PlayerSeasonStatsDto>> getPlayerStats(@PathVariable Long id) {
        return ResponseEntity.ok().body(playerService.getPlayerStats(id));
    }
}