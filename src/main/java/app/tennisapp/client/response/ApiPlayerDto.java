package app.tennisapp.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiPlayerDto(
        @JsonProperty("player_key") String playerKey,
        @JsonProperty("player_name") String playerName,
        @JsonProperty("player_country") String playerCountry,
        @JsonProperty("player_bday") String playerBday,
        @JsonProperty("player_logo") String playerLogo,
        List<ApiPlayerStatsDto> stats
) {}