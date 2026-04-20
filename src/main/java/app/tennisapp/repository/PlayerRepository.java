package app.tennisapp.repository;

import app.tennisapp.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByExternalId(Long externalId);

    List<Player> findByFullNameContainingIgnoreCase(String name);

    List<Player> findByNationality(String nationality);
}