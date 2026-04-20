package app.tennisapp.repository;

import app.tennisapp.entity.RankingEntry;
import app.tennisapp.entity.RankingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface RankingRepository extends JpaRepository<RankingEntry, Long> {
    @Query("SELECT r FROM RankingEntry r LEFT JOIN FETCH r.player WHERE r.rankingType = :type ORDER BY r.position ASC")
    List<RankingEntry> findByRankingTypeOrderByPositionAsc(@Param("type") RankingType type);

    void deleteByRankingType(RankingType rankingType);
}