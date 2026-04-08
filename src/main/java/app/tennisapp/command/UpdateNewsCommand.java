package app.tennisapp.command;

import jakarta.validation.constraints.Size;

public record UpdateNewsCommand(
        @Size(max = 255, message = "Title cannot exceed 255 characters")
        String title,

        String content,

        String imageUrl
) {
}