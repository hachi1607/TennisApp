package app.tennisapp.repository;

import app.tennisapp.entity.FavoritePlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavoritePlayerRepository extends JpaRepository<FavoritePlayer, FavoritePlayer.FavoritePlayerId> {
    // FavoritePlayerRepository.java
    @Query("SELECT f FROM FavoritePlayer f JOIN FETCH f.user JOIN FETCH f.player WHERE f.user.id = :userId")
    List<FavoritePlayer> findByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndPlayerId(Long userId, Long playerId);

    void deleteByUserIdAndPlayerId(Long userId, Long playerId);
}