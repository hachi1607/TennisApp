package app.tennisapp.controller;

import app.tennisapp.dto.MatchDto;
import app.tennisapp.service.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ResponseEntity<Page<MatchDto>> getMatches(
            @RequestParam(required = false) Boolean live,
            @RequestParam(required = false) Long playerId,
            @RequestParam(required = false) Long firstPlayerId,
            @RequestParam(required = false) Long secondPlayerId,
            @RequestParam(required = false) Long tournamentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @PageableDefault(sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(matchService.getMatches(
                live, playerId, firstPlayerId, secondPlayerId,
                tournamentId, dateFrom, dateTo, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchDto> getMatchById(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getMatchById(id));
    }
}