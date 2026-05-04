package app.tennisapp.repository;

import app.tennisapp.entity.Match;
import io.micrometer.common.lang.NonNull;
import io.micrometer.common.lang.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long>, JpaSpecificationExecutor<Match> {
    Optional<Match> findByExternalId(Long externalId);

    @NonNull
    @EntityGraph("Match.withRelations")
    Page<Match> findAll(@Nullable Specification<Match> spec, @NonNull Pageable pageable);

    @Modifying // dla queries od modyfikowania danych zamiast odczytu
    @Query("UPDATE Match m SET m.isLive = false WHERE m.isLive = true")
    void resetAllLiveMatches();

    @Query("SELECT m FROM Match m LEFT JOIN FETCH m.firstPlayer LEFT JOIN FETCH m.secondPlayer LEFT JOIN FETCH m.tournament WHERE m.id = :id")
    Optional<Match> getMatchByIdWithPlayers(@Param("id") Long id);
}