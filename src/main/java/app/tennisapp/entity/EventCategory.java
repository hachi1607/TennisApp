package app.tennisapp.entity;

import java.util.Arrays;

public enum EventCategory {
    ATP_SINGLES("Atp Singles"),
    ATP_DOUBLES("Atp Doubles"),
    WTA_SINGLES("Wta Singles"),
    WTA_DOUBLES("Wta Doubles");

    private final String apiValue;

    EventCategory(String apiValue) {
        this.apiValue = apiValue;
    }

    public static EventCategory fromApiString(String value) {
        return Arrays.stream(values())
                .filter(c -> c.apiValue.equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }
}