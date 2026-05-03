package app.tennisapp.client;

import app.tennisapp.client.response.*;
import app.tennisapp.config.ApiTennisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiTennisClient {
    private final RestClient apiTennisRestClient;
    private final ApiTennisProperties apiTennisProperties;

    public List<ApiTournamentDto> fetchTournaments() {
        ApiTournamentsResponse response = Optional.ofNullable(apiTennisRestClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .queryParam("method", "get_tournaments")
                                .queryParam("APIkey", apiTennisProperties.getKey())
                                .build())
                        .retrieve()
                        .body(ApiTournamentsResponse.class))
                .orElseThrow(() -> new IllegalStateException("No response from API Tennis for tournaments"));

        validateSuccess(response.success(), "tournaments");

        if (response.result() == null || response.result().isEmpty()) {
            throw new IllegalStateException("No tournaments returned from API Tennis");
        }
        return response.result();
    }

    public List<ApiMatchDto> fetchFixtures(LocalDate dateStart, LocalDate dateStop) {
        ApiMatchesResponse response = Optional.ofNullable(apiTennisRestClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .queryParam("method", "get_fixtures")
                                .queryParam("APIkey", apiTennisProperties.getKey())
                                .queryParam("date_start", dateStart.toString())
                                .queryParam("date_stop", dateStop.toString())
                                .build())
                        .retrieve()
                        .body(ApiMatchesResponse.class))
                .orElseThrow(() -> new IllegalStateException("No response from API Tennis for fixtures"));

        validateSuccess(response.success(), "fixtures");

        if (response.result() == null || response.result().isEmpty()) {
            throw new IllegalStateException("No fixtures returned from API Tennis");
        }
        return response.result();
    }

    public List<ApiMatchDto> fetchLivescores() {
        ApiMatchesResponse response = Optional.ofNullable(apiTennisRestClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .queryParam("method", "get_livescore")
                                .queryParam("APIkey", apiTennisProperties.getKey())
                                .build())
                        .retrieve()
                        .body(ApiMatchesResponse.class))
                .orElseThrow(() -> new IllegalStateException("No response from API Tennis for livescores"));

        validateSuccess(response.success(), "livescores");

        if (response.result() == null || response.result().isEmpty()) {
            return Collections.emptyList();
        }
        return response.result();
    }

    public List<ApiStandingDto> fetchStandings(String eventType) {
        ApiStandingsResponse response = Optional.ofNullable(apiTennisRestClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .queryParam("method", "get_standings")
                                .queryParam("APIkey", apiTennisProperties.getKey())
                                .queryParam("event_type", eventType)
                                .build())
                        .retrieve()
                        .body(ApiStandingsResponse.class))
                .orElseThrow(() -> new IllegalStateException("No response from API Tennis for standings"));

        validateSuccess(response.success(), "standings");

        if (response.result() == null || response.result().isEmpty()) {
            throw new IllegalStateException("No standings returned from API Tennis");
        }
        return response.result();
    }

    public ApiPlayerDto fetchPlayer(Long playerKey) {
        ApiPlayersResponse response = Optional.ofNullable(apiTennisRestClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .queryParam("method", "get_players")
                                .queryParam("APIkey", apiTennisProperties.getKey())
                                .queryParam("player_key", playerKey)
                                .build())
                        .retrieve()
                        .body(ApiPlayersResponse.class))
                .orElseThrow(() -> new IllegalStateException("No response from API Tennis for player " + playerKey));

        validateSuccess(response.success(), "player " + playerKey);

        if (response.result() == null || response.result().isEmpty()) {
            throw new IllegalStateException("No player returned from API Tennis for key " + playerKey);
        }

        ApiPlayerDto dto = response.result().getFirst();
        if (dto.playerKey() == null) {
            throw new IllegalStateException("External API returned invalid player data");
        }
        return dto;
    }

    private void validateSuccess(Integer success, String context) {
        if (success == null || success != 1) {
            log.error("API Tennis returned success={} for {}", success, context);
            throw new IllegalStateException("External API error for " + context);
        }
    }
}