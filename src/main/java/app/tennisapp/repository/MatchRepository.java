package app.tennisapp.repository;

import app.tennisapp.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    Optional<Match> findByExternalId(Long externalId);

    boolean existsByExternalId(Long externalId);

    List<Match> findByDateBetweenOrderByDateAscTimeAsc(LocalDate start, LocalDate end);

    List<Match> findByTournamentIdOrderByDateDesc(Long tournamentId);

    List<Match> findByFirstPlayerIdOrSecondPlayerIdOrderByDateDesc(Long firstPlayerId, Long secondPlayerId);

    List<Match> findByIsLiveTrue();

    List<Match> findBySeason(String season);
}