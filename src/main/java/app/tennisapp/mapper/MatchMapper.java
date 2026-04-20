package app.tennisapp.mapper;

import app.tennisapp.dto.MatchDto;
import app.tennisapp.entity.Match;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchMapper {
    private final ObjectMapper objectMapper;

    public MatchDto toDto(Match match) {
        return new MatchDto(
                match.getId(),
                match.getExternalId(),
                match.getFirstPlayer().getId(),
                match.getFirstPlayer().getFullName(),
                match.getSecondPlayer().getId(),
                match.getSecondPlayer().getFullName(),
                match.getTournament().getId(),
                match.getTournament().getName(),
                match.getSeason(),
                match.getDate(),
                match.getTime(),
                match.getFinalResult(),
                match.getGameResult(),
                match.getEventServe(),
                match.getWinner(),
                match.getStatus(),
                match.getRound(),
                match.isLive(),
                match.isQualification(),
                parseScores(match.getScoresJson())
        );
    }

    public List<MatchDto> toDto(List<Match> matches) {
        return matches.stream()
                .map(this::toDto)
                .toList();
    }

    private JsonNode parseScores(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            return null;
        }
    }
}