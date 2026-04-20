package app.tennisapp.mapper;

import app.tennisapp.client.response.ApiPlayerDto;
import app.tennisapp.client.response.ApiPlayerStatsDto;
import app.tennisapp.entity.Player;
import app.tennisapp.entity.PlayerSeasonStats;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static app.tennisapp.mapper.ApiParseUtils.parseBirthDate;
import static app.tennisapp.mapper.ApiParseUtils.parseInt;

@Component
public class ApiPlayerMapper {

    public Player toEntity(ApiPlayerDto dto, Long externalId) {
        return Player.builder()
                .externalId(externalId)
                .fullName(dto.playerName())
                .nationality(dto.playerCountry())
                .imageUrl(dto.playerLogo())
                .birthDate(parseBirthDate(dto.playerBday()))
                .lastSyncedAt(LocalDateTime.now())
                .build();
    }

    public Player updateEntity(Player existing, ApiPlayerDto dto) {
        return existing.toBuilder()
                .fullName(dto.playerName())
                .nationality(dto.playerCountry())
                .imageUrl(dto.playerLogo())
                .birthDate(parseBirthDate(dto.playerBday()))
                .lastSyncedAt(LocalDateTime.now())
                .build();
    }

    public PlayerSeasonStats toStatsEntity(ApiPlayerStatsDto dto, Player player) {
        return PlayerSeasonStats.builder()
                .player(player)
                .season(dto.season())
                .type(dto.type())
                .rank(parseInt(dto.rank()))
                .titles(parseInt(dto.titles()))
                .matchesWon(parseInt(dto.matchesWon()))
                .matchesLost(parseInt(dto.matchesLost()))
                .hardWon(parseInt(dto.hardWon()))
                .hardLost(parseInt(dto.hardLost()))
                .clayWon(parseInt(dto.clayWon()))
                .clayLost(parseInt(dto.clayLost()))
                .grassWon(parseInt(dto.grassWon()))
                .grassLost(parseInt(dto.grassLost()))
                .build();
    }

    public PlayerSeasonStats updateStatsEntity(PlayerSeasonStats existing, ApiPlayerStatsDto dto) {
        return existing.toBuilder()
                .rank(parseInt(dto.rank()))
                .titles(parseInt(dto.titles()))
                .matchesWon(parseInt(dto.matchesWon()))
                .matchesLost(parseInt(dto.matchesLost()))
                .hardWon(parseInt(dto.hardWon()))
                .hardLost(parseInt(dto.hardLost()))
                .clayWon(parseInt(dto.clayWon()))
                .clayLost(parseInt(dto.clayLost()))
                .grassWon(parseInt(dto.grassWon()))
                .grassLost(parseInt(dto.grassLost()))
                .build();
    }
}