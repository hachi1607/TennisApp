package app.tennisapp.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorMessage(String message, LocalDateTime timestamp, List<ValidationError> validationErrors) {

    public ErrorMessage(String message) {
        this(message, LocalDateTime.now(), null);
    }

    public ErrorMessage(String message, List<ValidationError> validationErrors) {
        this(message, LocalDateTime.now(), validationErrors);
    }
}