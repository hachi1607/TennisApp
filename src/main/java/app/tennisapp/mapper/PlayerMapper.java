package app.tennisapp.mapper;

import app.tennisapp.dto.PlayerDto;
import app.tennisapp.dto.PlayerSeasonStatsDto;
import app.tennisapp.entity.Player;
import app.tennisapp.entity.PlayerSeasonStats;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlayerMapper {
    public PlayerDto toDto(Player player) {
        return new PlayerDto(
                player.getId(),
                player.getExternalId(),
                player.getFullName(),
                player.getNationality(),
                player.getBirthDate(),
                player.getBio(),
                player.getImageUrl()
        );
    }

    public List<PlayerDto> toDto(List<Player> players) {
        return players.stream()
                .map(this::toDto)
                .toList();
    }

    public PlayerSeasonStatsDto toStatsDto(PlayerSeasonStats stats) {
        return new PlayerSeasonStatsDto(
                stats.getId(),
                stats.getPlayer().getId(),
                stats.getSeason(),
                stats.getType(),
                stats.getRank(),
                stats.getTitles(),
                stats.getMatchesWon(),
                stats.getMatchesLost(),
                stats.getHardWon(),
                stats.getHardLost(),
                stats.getClayWon(),
                stats.getClayLost(),
                stats.getGrassWon(),
                stats.getGrassLost()
        );
    }

    public List<PlayerSeasonStatsDto> toStatsDto(List<PlayerSeasonStats> statsList) {
        return statsList.stream()
                .map(this::toStatsDto)
                .toList();
    }
}