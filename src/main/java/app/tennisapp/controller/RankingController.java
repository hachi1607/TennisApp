package app.tennisapp.controller;

import app.tennisapp.dto.RankingEntryDto;
import app.tennisapp.entity.RankingType;
import app.tennisapp.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/ranking")
@RestController
public class RankingController {
    private final RankingService rankingService;

    @GetMapping("/{type}")
    public ResponseEntity<List<RankingEntryDto>> getRankingByType(@PathVariable RankingType type) {
        return ResponseEntity.ok().body(rankingService.getRankingByType(type));
    }
}