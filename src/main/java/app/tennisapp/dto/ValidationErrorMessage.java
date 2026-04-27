package app.tennisapp.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ValidationErrorMessage(String message, int status, LocalDateTime timestamp, List<ValidationError> errors) {

    public ValidationErrorMessage(String message, int status, List<ValidationError> errors) {
        this(message, status, LocalDateTime.now(), errors);
    }
}