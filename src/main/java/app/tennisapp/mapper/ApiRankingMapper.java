package app.tennisapp.mapper;

import app.tennisapp.client.response.ApiStandingDto;
import app.tennisapp.entity.Player;
import app.tennisapp.entity.RankingEntry;
import app.tennisapp.entity.RankingType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static app.tennisapp.mapper.ApiParseUtils.parseInt;

@Component
public class ApiRankingMapper {

    public RankingEntry toEntity(ApiStandingDto dto, Player player, RankingType type, LocalDate date) {
        return RankingEntry.builder()
                .player(player)
                .position(parseInt(dto.place()))
                .points(parseInt(dto.points()))
                .movement(dto.movement())
                .rankingType(type)
                .rankingDate(date)
                .build();
    }

    public Player toPlayerSkeleton(ApiStandingDto dto, Long externalId) {
        return Player.builder()
                .externalId(externalId)
                .fullName(dto.player())
                .nationality(dto.country())
                .build();
    }
}