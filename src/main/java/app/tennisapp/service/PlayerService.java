package app.tennisapp.service;

import app.tennisapp.dto.PlayerDto;
import app.tennisapp.dto.PlayerSeasonStatsDto;
import app.tennisapp.entity.Player;
import app.tennisapp.exception.ResourceNotFoundException;
import app.tennisapp.mapper.PlayerMapper;
import app.tennisapp.repository.PlayerRepository;
import app.tennisapp.repository.PlayerSeasonStatsRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerSeasonStatsRepository playerSeasonStatsRepository;
    private final PlayerMapper playerMapper;
    private final SyncService syncService;
    private final EntityManager entityManager;

    public Page<PlayerDto> getPlayers(String name, String nationality, Pageable pageable) {
        log.debug("Fetching players name={}, nationality={}, page={}, size={}",
                name, nationality, pageable.getPageNumber(), pageable.getPageSize());

        if (name != null && nationality != null) {
            return playerRepository.findByFullNameContainingIgnoreCaseAndNationalityIgnoreCase(name, nationality, pageable)
                    .map(playerMapper::toDto);
        }
        if (name != null) {
            return playerRepository.findByFullNameContainingIgnoreCase(name, pageable)
                    .map(playerMapper::toDto);
        }
        if (nationality != null) {
            return playerRepository.findByNationalityIgnoreCase(nationality, pageable)
                    .map(playerMapper::toDto);
        }
        return playerRepository.findAllPaged(pageable)
                .map(playerMapper::toDto);
    }

    public PlayerDto getPlayerById(Long id) {
        log.debug("Fetching player id={}", id);
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Player not found: id={}", id);
                    return new ResourceNotFoundException("Player not found: " + id);
                });

        if (player.getLastSyncedAt() == null) {
            log.info("Player id={} is incomplete, triggering sync", id);
            syncService.syncPlayer(player.getExternalId());
            entityManager.refresh(player); // odświeża entity playera po zsynchronizowaniu
        }

        return playerMapper.toDto(player);
    }

    public List<PlayerSeasonStatsDto> getPlayerStats(Long playerId) {
        log.debug("Fetching stats for player id={}", playerId);
        if (!playerRepository.existsById(playerId)) {
            log.warn("Player not found: id={}", playerId);
            throw new ResourceNotFoundException("Player not found: " + playerId);
        }
        return playerMapper.toStatsDto(playerSeasonStatsRepository.findByPlayerId(playerId));
    }
}