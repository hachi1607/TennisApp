package app.tennisapp.mapper;

import app.tennisapp.dto.TournamentDto;
import app.tennisapp.entity.Tournament;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TournamentMapper {
    public TournamentDto toDto(Tournament tournament) {
        return new TournamentDto(
                tournament.getId(),
                tournament.getExternalId(),
                tournament.getName(),
                tournament.getEventCategory()
        );
    }

    public List<TournamentDto> toDto(List<Tournament> tournaments) {
        return tournaments.stream()
                .map(this::toDto)
                .toList();
    }
}