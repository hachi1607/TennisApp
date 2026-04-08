package app.tennisapp.mapper;

import app.tennisapp.dto.MatchDto;
import app.tennisapp.entity.Match;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MatchMapper {
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
                match.getScoresJson()
        );
    }

    public List<MatchDto> toDto(List<Match> matches) {
        return matches.stream()
                .map(this::toDto)
                .toList();
    }
}