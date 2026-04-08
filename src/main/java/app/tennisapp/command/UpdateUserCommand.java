package app.tennisapp.command;

import jakarta.validation.constraints.Email;

public record UpdateUserCommand(
        @Email(message = "Email must be valid")
        String email,

        String password
) {
}