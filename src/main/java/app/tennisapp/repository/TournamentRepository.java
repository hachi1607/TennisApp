package app.tennisapp.repository;

import app.tennisapp.entity.EventCategory;
import app.tennisapp.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    Optional<Tournament> findByExternalId(Long externalId);

    List<Tournament> findByEventCategory(EventCategory eventCategory);

    List<Tournament> findByNameContainingIgnoreCase(String name);
}