package app.tennisapp.client;

import app.tennisapp.config.ApiTennisProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class ApiTennisClient {
    private final RestClient apiTennisRestClient;
    private final ApiTennisProperties apiTennisProperties;

    public Optional<JsonNode> fetchTournaments() {
        return executeListRequest(
                uriBuilder -> uriBuilder
                        .queryParam("method", "get_tournaments")
                        .queryParam("APIkey", apiTennisProperties.getKey())
                        .build());
    }

    public Optional<JsonNode> fetchFixtures(LocalDate dateStart, LocalDate dateStop) {
        return executeListRequest(
                uriBuilder -> uriBuilder
                        .queryParam("method", "get_fixtures")
                        .queryParam("APIkey", apiTennisProperties.getKey())
                        .queryParam("date_start", dateStart.toString())
                        .queryParam("date_stop", dateStop.toString())
                        .build());
    }

    public Optional<JsonNode> fetchLivescores() {
        return executeListRequest(
                uriBuilder -> uriBuilder
                        .queryParam("method", "get_livescore")
                        .queryParam("APIkey", apiTennisProperties.getKey())
                        .build());
    }

    public Optional<JsonNode> fetchStandings(String eventType) {
        return executeListRequest(
                uriBuilder -> uriBuilder
                        .queryParam("method", "get_standings")
                        .queryParam("APIkey", apiTennisProperties.getKey())
                        .queryParam("event_type", eventType)
                        .build());
    }

    public Optional<JsonNode> fetchPlayer(Long playerKey) {
        return executeSingleRequest(uriBuilder -> uriBuilder
                .queryParam("method", "get_players")
                .queryParam("APIkey", apiTennisProperties.getKey())
                .queryParam("player_key", playerKey)
                .build());
    }

    private Optional<JsonNode> executeListRequest(Function<UriBuilder, URI> uriFunction) {
        return Optional.ofNullable(apiTennisRestClient.get()
                        .uri(uriFunction)
                        .retrieve()
                        .body(JsonNode.class))
                .filter(response -> response.path("success").asInt() == 1)
                .map(response -> response.path("result"))
                .filter(result -> result.isArray() && !result.isEmpty()); // dla zwracanej pojedynczej encji
    }

    private Optional<JsonNode> executeSingleRequest(Function<UriBuilder, URI> uriFunction) {
        return Optional.ofNullable(apiTennisRestClient.get()
                        .uri(uriFunction)
                        .retrieve()
                        .body(JsonNode.class))
                .filter(response -> response.path("success").asInt() == 1)
                .map(response -> response.path("result"))
                .filter(result -> result.isArray() && !result.isEmpty())
                .map(result -> result.get(0)); // dla zwracanej więcej niż jednej encji na raz
    }
}