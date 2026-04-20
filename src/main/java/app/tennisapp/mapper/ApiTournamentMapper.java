package app.tennisapp.mapper;

import app.tennisapp.client.response.ApiTournamentDto;
import app.tennisapp.entity.EventCategory;
import app.tennisapp.entity.Tournament;
import org.springframework.stereotype.Component;

@Component
public class ApiTournamentMapper {

    public Tournament toEntity(ApiTournamentDto dto, Long externalId, EventCategory category) {
        return Tournament.builder()
                .externalId(externalId)
                .name(dto.tournamentName())
                .eventCategory(category)
                .build();
    }

    public Tournament updateEntity(Tournament existing, ApiTournamentDto dto, EventCategory category) {
        return existing.toBuilder()
                .name(dto.tournamentName())
                .eventCategory(category)
                .build();
    }
}