package app.tennisapp.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiTournamentDto(
        @JsonProperty("tournament_key") String tournamentKey,
        @JsonProperty("tournament_name") String tournamentName,
        @JsonProperty("event_type_key") String eventTypeKey,
        @JsonProperty("event_type_type") String eventTypeType
) {}