package app.tennisapp.dto;

import java.time.LocalDate;

public record PlayerDto(
        Long id,
        Long externalId,
        String fullName,
        String nationality,
        LocalDate birthDate,
        String imageUrl
) {
}