package app.tennisapp.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiStandingDto(
        String place,
        String player,
        @JsonProperty("player_key") String playerKey,
        String league,
        String movement,
        String country,
        String points
) {}