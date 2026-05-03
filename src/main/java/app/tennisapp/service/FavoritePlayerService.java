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

    @Transactional(readOnly = true)
    public List<FavoritePlayerDto> getUserFavorites(String email) {
        User user = getUserByEmail(email);
        return favoritePlayerMapper.toDto(favoritePlayerRepository.findByUserId(user.getId()));
    }

    @Transactional
    public FavoritePlayerDto addFavorite(String email, Long playerId) {
        User user = getUserByEmail(email);
        log.info("Adding favorite, userId={}, playerId={}", user.getId(), playerId);

        if (favoritePlayerRepository.existsByUserIdAndPlayerId(user.getId(), playerId)) {
            log.warn("Player id={} already in favorites for user id={}", playerId, user.getId());
            throw new IllegalStateException("Player already in favorites");
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found: " + playerId));

        FavoritePlayer favorite = FavoritePlayer.builder()
                .user(user)
                .player(player)
                .build();

        log.info("Favorite added, userId={}, playerId={}", user.getId(), playerId);
        return favoritePlayerMapper.toDto(favoritePlayerRepository.save(favorite));
    }

    @Transactional
    public void removeFavorite(String email, Long playerId) {
        User user = getUserByEmail(email);
        log.info("Removing favorite, userId={}, playerId={}", user.getId(), playerId);

        if (!favoritePlayerRepository.existsByUserIdAndPlayerId(user.getId(), playerId)) {
            log.warn("Favorite not found, userId={}, playerId={}", user.getId(), playerId);
            throw new ResourceNotFoundException("Favorite not found for user " + user.getId() + " and player " + playerId);
        }

        favoritePlayerRepository.deleteByUserIdAndPlayerId(user.getId(), playerId);
        log.info("Favorite removed, userId={}, playerId={}", user.getId(), playerId);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}