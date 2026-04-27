package app.tennisapp.mapper;

import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor
public class ApiParseUtils {
    public static Long parseLong(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            long parsed = Long.parseLong(value);
            return parsed == 0L ? null : parsed;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static int parseInt(String value) {
        if (value == null || value.isBlank()) return 0;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static LocalDate parseMatchDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw);
        } catch (Exception e) {
            return null;
        }
    }

    public static LocalDate parseBirthDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isAbbreviatedName(String name) {
        if (name == null || name.isBlank()) return false;
        return name.matches("^[A-Z]\\. .+");
    }
}