package app.tennisapp.dto;

import java.time.LocalDateTime;

public record ErrorMessage(String message, int status, LocalDateTime timestamp) {

    public ErrorMessage(String message, int status) {
        this(message, status, LocalDateTime.now());
    }
}