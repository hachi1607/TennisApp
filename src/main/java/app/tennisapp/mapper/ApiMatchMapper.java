package app.tennisapp.mapper;

import app.tennisapp.client.response.ApiMatchDto;
import app.tennisapp.entity.Match;
import app.tennisapp.entity.Player;
import app.tennisapp.entity.Tournament;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static app.tennisapp.mapper.ApiParseUtils.parseMatchDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiMatchMapper {
    private final ObjectMapper objectMapper;

    public Match toEntity(ApiMatchDto dto, Long externalId, Player firstPlayer, Player secondPlayer, Tournament tournament) {
        return Match.builder()
                .externalId(externalId)
                .firstPlayer(firstPlayer)
                .secondPlayer(secondPlayer)
                .tournament(tournament)
                .season(dto.tournamentSeason())
                .date(parseMatchDate(dto.eventDate()))
                .time(dto.eventTime())
                .finalResult(dto.eventFinalResult())
                .gameResult(dto.eventGameResult())
                .eventServe(dto.eventServe())
                .winner(dto.eventWinner())
                .status(dto.eventStatus())
                .round(dto.tournamentRound())
                .isLive(!"0".equals(dto.eventLive()))
                .qualification("True".equalsIgnoreCase(dto.eventQualification()))
                .scoresJson(serializeScores(dto))
                .lastSyncedAt(LocalDateTime.now())
                .build();
    }

    public Match updateEntity(Match existing, ApiMatchDto dto, Player firstPlayer, Player secondPlayer, Tournament tournament) {
        return existing.toBuilder()
                .firstPlayer(firstPlayer)
                .secondPlayer(secondPlayer)
                .tournament(tournament)
                .season(dto.tournamentSeason())
                .date(parseMatchDate(dto.eventDate()))
                .time(dto.eventTime())
                .finalResult(dto.eventFinalResult())
                .gameResult(dto.eventGameResult())
                .eventServe(dto.eventServe())
                .winner(dto.eventWinner())
                .status(dto.eventStatus())
                .round(dto.tournamentRound())
                .isLive(!"0".equals(dto.eventLive()))
                .qualification("True".equalsIgnoreCase(dto.eventQualification()))
                .scoresJson(serializeScores(dto))
                .lastSyncedAt(LocalDateTime.now())
                .build();
    }

    private String serializeScores(ApiMatchDto dto) {
        if (dto.scores() == null || dto.scores().isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(dto.scores());
        } catch (Exception e) {
            log.warn("Could not serialize scores for match {}", dto.eventKey());
            return null;
        }
    }
}