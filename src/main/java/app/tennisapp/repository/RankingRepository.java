package app.tennisapp.repository;

import app.tennisapp.entity.RankingEntry;
import app.tennisapp.entity.RankingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RankingRepository extends JpaRepository<RankingEntry, Long> {
    List<RankingEntry> findByRankingTypeOrderByPositionAsc(RankingType rankingType);

    List<RankingEntry> findByRankingTypeAndRankingDateOrderByPositionAsc(RankingType rankingType, LocalDate rankingDate);

    List<RankingEntry> findByPlayerId(Long playerId);

    void deleteByRankingTypeAndRankingDate(RankingType rankingType, LocalDate rankingDate);
}