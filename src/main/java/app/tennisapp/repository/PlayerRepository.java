package app.tennisapp.repository;

import app.tennisapp.entity.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByExternalId(Long externalId);

    Page<Player> findByFullNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Player> findByNationalityIgnoreCase(String nationality, Pageable pageable);

    Page<Player> findByFullNameContainingIgnoreCaseAndNationalityIgnoreCase(String name, String nationality, Pageable pageable);

    @Query(
            value = "SELECT p FROM Player p",
            countQuery = "SELECT COUNT(p) FROM Player p"
    )
    Page<Player> findAllPaged(Pageable pageable);
}
