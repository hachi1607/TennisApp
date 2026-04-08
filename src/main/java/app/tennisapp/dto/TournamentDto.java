package app.tennisapp.dto;

import app.tennisapp.entity.EventCategory;

public record TournamentDto(
        Long id,
        Long externalId,
        String name,
        EventCategory eventCategory
) {
}