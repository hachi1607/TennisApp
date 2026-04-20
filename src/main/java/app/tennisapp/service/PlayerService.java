package app.tennisapp.service;

import app.tennisapp.dto.PlayerDto;
import app.tennisapp.dto.PlayerSeasonStatsDto;
import app.tennisapp.entity.Player;
import app.tennisapp.exception.ResourceNotFoundException;
import app.tennisapp.mapper.PlayerMapper;
import app.tennisapp.repository.PlayerRepository;
import app.tennisapp.repository.PlayerSeasonStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerSeasonStatsRepository playerSeasonStatsRepository;
    private final PlayerMapper playerMapper;

    public List<PlayerDto> getAllPlayers() {
        return playerMapper.toDto(playerRepository.findAll());
    }

    public PlayerDto getPlayerById(Long id) {
        Optional<Player> player = playerRepository.findById(id);
        return playerMapper.toDto(player.orElseThrow(() -> new ResourceNotFoundException("Player not found: " + id)));
    }

    public List<PlayerDto> searchPlayersByName(String name) {
        return playerMapper.toDto(playerRepository.findByFullNameContainingIgnoreCase(name));
    }

    public List<PlayerDto> getPlayersByNationality(String nationality) {
        return playerMapper.toDto(playerRepository.findByNationality(nationality));
    }

    public List<PlayerSeasonStatsDto> getPlayerStats(Long playerId) {
        if (!playerRepository.existsById(playerId)) {
            throw new ResourceNotFoundException("Player not found: " + playerId);
        }
        return playerMapper.toStatsDto(playerSeasonStatsRepository.findByPlayerId(playerId));
    }
}