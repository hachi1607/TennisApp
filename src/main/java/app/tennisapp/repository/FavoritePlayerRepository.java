package app.tennisapp.repository;

import app.tennisapp.entity.FavoritePlayer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoritePlayerRepository extends JpaRepository<FavoritePlayer, FavoritePlayer.FavoritePlayerId> {
    List<FavoritePlayer> findByUserId(Long userId);

    boolean existsByUserIdAndPlayerId(Long userId, Long playerId);

    void deleteByUserIdAndPlayerId(Long userId, Long playerId);
}