package app.tennisapp.dto;

import app.tennisapp.entity.Role;

import java.time.LocalDateTime;

public record UserDto(
        Long id,
        String email,
        Role role,
        LocalDateTime createdAt
) {
}