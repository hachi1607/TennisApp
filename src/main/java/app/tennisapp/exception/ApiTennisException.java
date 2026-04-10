package app.tennisapp.exception;

public class ApiTennisException extends RuntimeException {

    public ApiTennisException(String message) {
        super(message);
    }

    public ApiTennisException() {
    }
}