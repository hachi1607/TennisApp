package app.tennisapp.service;

import app.tennisapp.dto.FavoritePlayerDto;
import app.tennisapp.entity.FavoritePlayer;
import app.tennisapp.entity.Player;
import app.tennisapp.entity.User;
import app.tennisapp.exception.ResourceNotFoundException;
import app.tennisapp.mapper.FavoritePlayerMapper;
import app.tennisapp.repository.FavoritePlayerRepository;
import app.tennisapp.repository.PlayerRepository;
import app.tennisapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoritePlayerService {
    private final FavoritePlayerRepository favoritePlayerRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final FavoritePlayerMapper favoritePlayerMapper;

    public List<FavoritePlayerDto> getUserFavorites(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }
        return favoritePlayerMapper.toDto(favoritePlayerRepository.findByUserId(userId));
    }

    @Transactional
    public FavoritePlayerDto addFavorite(Long userId, Long playerId) {
        log.info("Adding favorite, userId={}, playerId={}", userId, playerId);
        if (favoritePlayerRepository.existsByUserIdAndPlayerId(userId, playerId)) {
            log.warn("Player id={} already in favorites for user id={}", playerId, userId);
            throw new IllegalStateException("Player already in favorites");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found: " + playerId));

        FavoritePlayer favorite = FavoritePlayer.builder()
                .user(user)
                .player(player)
                .build();

        log.info("Favorite added, userId={}, playerId={}", userId, playerId);
        return favoritePlayerMapper.toDto(favoritePlayerRepository.save(favorite));
    }

    @Transactional
    public void removeFavorite(Long userId, Long playerId) {
        log.info("Removing favorite, userId={}, playerId={}", userId, playerId);
        if (!favoritePlayerRepository.existsByUserIdAndPlayerId(userId, playerId)) {
            log.warn("Favorite not found, userId={}, playerId={}", userId, playerId);
            throw new ResourceNotFoundException("Favorite not found for user " + userId + " and player " + playerId);
        }
        favoritePlayerRepository.deleteByUserIdAndPlayerId(userId, playerId);
        log.info("Favorite removed, userId={}, playerId={}", userId, playerId);
    }
}