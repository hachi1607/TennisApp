package app.tennisapp.controller;

import app.tennisapp.dto.FavoritePlayerDto;
import app.tennisapp.service.FavoritePlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/favorites")
@RestController
public class FavoritePlayerController {
    private final FavoritePlayerService favoritePlayerService;

    @GetMapping
    public ResponseEntity<List<FavoritePlayerDto>> getUserFavorites(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok().body(favoritePlayerService.getUserFavorites(userDetails.getUsername()));
    }

    @PostMapping
    public ResponseEntity<FavoritePlayerDto> addFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long playerId) {
        return ResponseEntity.status(201).body(favoritePlayerService.addFavorite(userDetails.getUsername(), playerId));
    }

    @DeleteMapping
    public ResponseEntity<Void> removeFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long playerId) {
        favoritePlayerService.removeFavorite(userDetails.getUsername(), playerId);
        return ResponseEntity.noContent().build();
    }
}