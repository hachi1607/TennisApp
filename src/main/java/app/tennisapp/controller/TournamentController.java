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
    public ResponseEntity<List<TournamentDto>> getAllTournaments() {
        return ResponseEntity.ok().body(tournamentService.getAllTournaments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TournamentDto> getTournamentById(@PathVariable Long id) {
        return ResponseEntity.ok().body(tournamentService.getTournamentById(id));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<TournamentDto>> getTournamentsByCategory(@PathVariable EventCategory category) {
        return ResponseEntity.ok().body(tournamentService.getTournamentsByCategory(category));
    }

    @GetMapping("/search")
    public ResponseEntity<List<TournamentDto>> searchTournamentsByName(@RequestParam String name) {
        return ResponseEntity.ok().body(tournamentService.searchTournamentsByName(name));
    }
}