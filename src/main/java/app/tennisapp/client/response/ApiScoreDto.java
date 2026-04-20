package app.tennisapp.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiScoreDto(
        @JsonProperty("score_first") String scoreFirst,
        @JsonProperty("score_second") String scoreSecond,
        @JsonProperty("score_set") String scoreSet
) {}