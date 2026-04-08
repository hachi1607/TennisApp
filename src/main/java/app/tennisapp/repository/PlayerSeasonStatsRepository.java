package app.tennisapp.repository;

import app.tennisapp.entity.PlayerSeasonStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerSeasonStatsRepository extends JpaRepository<PlayerSeasonStats, Long> {
    List<PlayerSeasonStats> findByPlayerId(Long playerId);

    List<PlayerSeasonStats> findByPlayerIdAndType(Long playerId, String type);

    Optional<PlayerSeasonStats> findByPlayerIdAndSeasonAndType(Long playerId, String season, String type);
}