package app.tennisapp.controller;

import app.tennisapp.dto.MatchDto;
import app.tennisapp.params.MatchFilterParams;
import app.tennisapp.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/matches")
@RestController
public class MatchController {
    private final MatchService matchService;

    @GetMapping
    public ResponseEntity<Page<MatchDto>> getMatches(
            MatchFilterParams params,
            @PageableDefault(sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok().body(matchService.getMatches(params, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchDto> getMatchById(@PathVariable Long id) {
        return ResponseEntity.ok().body(matchService.getMatchById(id));
    }
}