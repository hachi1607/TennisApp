package app.tennisapp.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiPlayerStatsDto(
        String season,
        String type,
        String rank,
        String titles,
        @JsonProperty("matches_won") String matchesWon,
        @JsonProperty("matches_lost") String matchesLost,
        @JsonProperty("hard_won") String hardWon,
        @JsonProperty("hard_lost") String hardLost,
        @JsonProperty("clay_won") String clayWon,
        @JsonProperty("clay_lost") String clayLost,
        @JsonProperty("grass_won") String grassWon,
        @JsonProperty("grass_lost") String grassLost
) {}
