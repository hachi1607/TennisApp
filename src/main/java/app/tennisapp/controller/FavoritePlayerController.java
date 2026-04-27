package app.tennisapp.controller;

import app.tennisapp.dto.FavoritePlayerDto;
import app.tennisapp.service.FavoritePlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/favorites")
@RestController
public class FavoritePlayerController {
    private final FavoritePlayerService favoritePlayerService;

    @PostMapping
    public ResponseEntity<FavoritePlayerDto> addFavorite(
            @RequestParam Long userId,
            @RequestParam Long playerId
    ) {
        return ResponseEntity.status(201).body(favoritePlayerService.addFavorite(userId, playerId));
    }

    @GetMapping
    public ResponseEntity<List<FavoritePlayerDto>> getUserFavorites(@RequestParam Long userId) {
        return ResponseEntity.ok().body(favoritePlayerService.getUserFavorites(userId));
    }

    @DeleteMapping
    public ResponseEntity<Void> removeFavorite(
            @RequestParam Long userId,
            @RequestParam Long playerId
    ) {
        favoritePlayerService.removeFavorite(userId, playerId);
        return ResponseEntity.noContent().build();
    }
}