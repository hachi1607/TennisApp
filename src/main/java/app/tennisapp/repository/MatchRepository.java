package app.tennisapp.repository;

import app.tennisapp.entity.Match;
import app.tennisapp.entity.RankingEntry;
import app.tennisapp.entity.RankingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long>, JpaSpecificationExecutor<Match> {
    Optional<Match> findByExternalId(Long externalId);
    // Testowane podejście z Specification
//    @Query("SELECT m FROM Match m LEFT JOIN FETCH m.firstPlayer LEFT JOIN FETCH m.secondPlayer LEFT JOIN FETCH m.tournament")
//    Page<Match> findAllWithPlayers(Pageable pageable);
//
//    @Query(
//            value = "SELECT m FROM Match m LEFT JOIN FETCH m.firstPlayer LEFT JOIN FETCH m.secondPlayer LEFT JOIN FETCH m.tournament",
//            countQuery = "SELECT COUNT(m) FROM Match m"
//    )
//    Page<Match> findAllPaged(Pageable pageable);
//
//    @Query("SELECT m FROM Match m LEFT JOIN FETCH m.firstPlayer LEFT JOIN FETCH m.secondPlayer LEFT JOIN FETCH m.tournament WHERE m.date BETWEEN :start AND :end ORDER BY m.date ASC, m.time ASC")
//    List<Match> findByDateBetweenOrderByDateAscTimeAsc(@Param("start") LocalDate start, @Param("end") LocalDate end);
//
//    @Query("SELECT m FROM Match m LEFT JOIN FETCH m.firstPlayer LEFT JOIN FETCH m.secondPlayer LEFT JOIN FETCH m.tournament WHERE m.tournament.id = :tournamentId ORDER BY m.date DESC")
//    List<Match> findByTournamentIdOrderByDateDesc(@Param("tournamentId") Long tournamentId);
//
//    @Query("SELECT m FROM Match m LEFT JOIN FETCH m.firstPlayer LEFT JOIN FETCH m.secondPlayer LEFT JOIN FETCH m.tournament WHERE m.firstPlayer.id = :playerId OR m.secondPlayer.id = :playerId ORDER BY m.date DESC")
//    List<Match> findByPlayerId(@Param("playerId") Long playerId);
//
//    @Query("SELECT m FROM Match m LEFT JOIN FETCH m.firstPlayer LEFT JOIN FETCH m.secondPlayer LEFT JOIN FETCH m.tournament WHERE (m.firstPlayer.id = :firstPlayerId AND m.secondPlayer.id = :secondPlayerId) OR (m.firstPlayer.id = :secondPlayerId AND m.secondPlayer.id = :firstPlayerId) ORDER BY m.date DESC")
//    List<Match> findByFirstPlayerIdAndSecondPlayerId(@Param("firstPlayerId") Long firstPlayerId, @Param("secondPlayerId") Long secondPlayerId);
//
//    @Query("SELECT m FROM Match m LEFT JOIN FETCH m.firstPlayer LEFT JOIN FETCH m.secondPlayer LEFT JOIN FETCH m.tournament WHERE m.isLive = true")
//    List<Match> findByIsLiveTrue();

    @Modifying // dla queries od modyfikowania danych zamiast odczytu
    @Query("UPDATE Match m SET m.isLive = false WHERE m.isLive = true")
    void resetAllLiveMatches();

    @Query("SELECT m FROM Match m LEFT JOIN FETCH m.firstPlayer LEFT JOIN FETCH m.secondPlayer LEFT JOIN FETCH m.tournament WHERE m.id = :id")
    Optional<Match> getMatchByIdWithPlayers(@Param("id") Long id);
}