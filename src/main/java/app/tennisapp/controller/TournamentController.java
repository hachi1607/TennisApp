package app.tennisapp.controller;

import app.tennisapp.dto.TournamentDto;
import app.tennisapp.entity.EventCategory;
import app.tennisapp.service.TournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/tournaments")
@RestController
public class TournamentController {
    private final TournamentService tournamentService;

    @GetMapping
    public ResponseEntity<List<TournamentDto>> getTournaments(
            @RequestParam(required = false) EventCategory category,
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok().body(tournamentService.getTournaments(category, name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TournamentDto> getTournamentById(@PathVariable Long id) {
        return ResponseEntity.ok().body(tournamentService.getTournamentById(id));
    }
}