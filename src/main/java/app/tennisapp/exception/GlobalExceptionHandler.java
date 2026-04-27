package app.tennisapp.exception;

import app.tennisapp.dto.ErrorMessage;
import app.tennisapp.dto.ValidationErrorMessage;
import app.tennisapp.dto.ValidationError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleResourceNotFound(ResourceNotFoundException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorMessage(exception.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorMessage> handleIllegalArgument(IllegalArgumentException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity
                .badRequest()
                .body(new ErrorMessage(exception.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorMessage> handleIllegalState(IllegalStateException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity
                .badRequest()
                .body(new ErrorMessage(exception.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorMessage> handleValidationException(MethodArgumentNotValidException exception) {
        log.error(exception.getMessage(), exception);
        List<ValidationError> validationErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> new ValidationError(e.getField(), e.getDefaultMessage()))
                .toList();
        return ResponseEntity
                .badRequest()
                .body(new ValidationErrorMessage("Validation failed", HttpStatus.BAD_REQUEST.value(), validationErrors));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorMessage> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        log.error("Type mismatch for parameter '{}': {}", exception.getName(), exception.getMessage());
        return ResponseEntity
                .badRequest()
                .body(new ErrorMessage("Invalid value for parameter '" + exception.getName() + "'", HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorMessage> handleNoResourceFound(NoResourceFoundException exception) {
        log.error("Endpoint not found: {}", exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorMessage("Endpoint not found: /" + exception.getResourcePath(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorMessage> handleApiTennisClientError(HttpClientErrorException exception) {
        log.error("API Tennis client error: {} - {}", exception.getStatusCode(), exception.getStatusText());
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorMessage("External API returned an error", HttpStatus.BAD_GATEWAY.value()));
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ErrorMessage> handleApiTennisServerError(HttpServerErrorException exception) {
        log.error("API Tennis server error: {}", exception.getStatusText());
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorMessage("External API is currently unavailable", HttpStatus.BAD_GATEWAY.value()));
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorMessage> handleApiTennisConnectionError(ResourceAccessException exception) {
        log.error("API Tennis connection error: {}", exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorMessage("Unable to connect to external API", HttpStatus.SERVICE_UNAVAILABLE.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleException(Exception exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorMessage("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}