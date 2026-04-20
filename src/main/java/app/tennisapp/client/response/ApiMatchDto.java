package app.tennisapp.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiMatchDto(
        @JsonProperty("event_key") String eventKey,
        @JsonProperty("event_date") String eventDate,
        @JsonProperty("event_time") String eventTime,
        @JsonProperty("event_first_player") String eventFirstPlayer,
        @JsonProperty("first_player_key") String firstPlayerKey,
        @JsonProperty("event_second_player") String eventSecondPlayer,
        @JsonProperty("second_player_key") String secondPlayerKey,
        @JsonProperty("event_final_result") String eventFinalResult,
        @JsonProperty("event_game_result") String eventGameResult,
        @JsonProperty("event_serve") String eventServe,
        @JsonProperty("event_winner") String eventWinner,
        @JsonProperty("event_status") String eventStatus,
        @JsonProperty("event_type_type") String eventTypeType,
        @JsonProperty("tournament_name") String tournamentName,
        @JsonProperty("tournament_key") String tournamentKey,
        @JsonProperty("tournament_round") String tournamentRound,
        @JsonProperty("tournament_season") String tournamentSeason,
        @JsonProperty("event_live") String eventLive,
        @JsonProperty("event_qualification") String eventQualification,
        List<ApiScoreDto> scores
) {}