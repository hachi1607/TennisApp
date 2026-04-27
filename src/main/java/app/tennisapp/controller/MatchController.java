package app.tennisapp.controller;

import app.tennisapp.dto.MatchDto;
import app.tennisapp.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/matches")
@RestController
public class MatchController {
    private final MatchService matchService;

    @GetMapping
    public ResponseEntity<Page<MatchDto>> getAllMatches(
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(matchService.getAllMatches(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchDto> getMatchById(@PathVariable Long id) {
        return ResponseEntity.ok().body(matchService.getMatchById(id));
    }

    @GetMapping("/live")
    public ResponseEntity<List<MatchDto>> getLiveMatches() {
        return ResponseEntity.ok().body(matchService.getLiveMatches());
    }

    @GetMapping("/range")
    public ResponseEntity<List<MatchDto>> getMatchesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ResponseEntity.ok().body(matchService.getMatchesByDateRange(start, end));
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<MatchDto>> getMatchesByPlayer(@PathVariable Long playerId) {
        return ResponseEntity.ok().body(matchService.getMatchesByPlayer(playerId));
    }

    @GetMapping("/players")
    public ResponseEntity<List<MatchDto>> getMatchesByPlayer(@RequestParam Long firstPlayerId, @RequestParam Long secondPlayerId) {
        return ResponseEntity.ok().body(matchService.getMatchesByPlayers(firstPlayerId, secondPlayerId));
    }

    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<List<MatchDto>> getMatchesByTournament(@PathVariable Long tournamentId) {
        return ResponseEntity.ok().body(matchService.getMatchesByTournament(tournamentId));
    }
}