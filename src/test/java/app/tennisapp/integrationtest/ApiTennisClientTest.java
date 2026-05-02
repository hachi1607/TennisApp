package app.tennisapp.integrationtest;

import app.tennisapp.client.ApiTennisClient;
import app.tennisapp.client.response.*;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@WireMockTest(httpPort = 8081)
class ApiTennisClientTest {
    @Autowired
    private ApiTennisClient apiTennisClient;

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("api-tennis.url", () -> "http://localhost:8081");
    }

    // fetchTournaments
    @Test
    void shouldFetchTournamentsSuccessfully() {
        stubFor(get(urlPathEqualTo("/tennis"))
                .withQueryParam("method", equalTo("get_tournaments"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("wiremock/tournaments_success.json")));

        List<ApiTournamentDto> result = apiTennisClient.fetchTournaments();

        assertThat(result).hasSize(1);
        assertEquals("1000", result.getFirst().tournamentKey());
        assertEquals("Wimbledon", result.getFirst().tournamentName());
        assertEquals("Atp Singles", result.getFirst().eventTypeType());
    }

    @Test
    void shouldThrowWhenTournamentsResultIsEmpty() {
        stubFor(get(urlPathEqualTo("/tennis"))
                .withQueryParam("method", equalTo("get_tournaments"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("wiremock/empty_result.json")));

        assertThatThrownBy(() -> apiTennisClient.fetchTournaments())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldThrowWhenTournamentsApiReturns500() {
        stubFor(get(urlPathEqualTo("/tennis"))
                .withQueryParam("method", equalTo("get_tournaments"))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> apiTennisClient.fetchTournaments())
                .isInstanceOf(Exception.class);
    }

    // fetchFixtures
    @Test
    void shouldFetchFixturesSuccessfully() {
        stubFor(get(urlPathEqualTo("/tennis"))
                .withQueryParam("method", equalTo("get_fixtures"))
                .withQueryParam("date_start", equalTo("2025-07-01"))
                .withQueryParam("date_stop", equalTo("2025-07-07"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("wiremock/fixtures_success.json")));

        List<ApiMatchDto> result = apiTennisClient.fetchFixtures(
                LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 7));

        assertThat(result).hasSize(1);
        assertEquals("9999", result.getFirst().eventKey());
        assertEquals("Novak Djokovic", result.getFirst().eventFirstPlayer());
        assertEquals("Wimbledon", result.getFirst().tournamentName());
        assertEquals("0", result.getFirst().eventLive());
    }

    @Test
    void shouldThrowWhenFixturesResultIsEmpty() {
        stubFor(get(urlPathEqualTo("/tennis"))
                .withQueryParam("method", equalTo("get_fixtures"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("wiremock/empty_result.json")));

        assertThatThrownBy(() -> apiTennisClient.fetchFixtures(
                LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 7)))
                .isInstanceOf(IllegalStateException.class);
    }

    // fetchLivescores
    @Test
    void shouldFetchLivescoresSuccessfully() {
        stubFor(get(urlPathEqualTo("/tennis"))
                .withQueryParam("method", equalTo("get_livescore"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("wiremock/livescores_success.json")));

        List<ApiMatchDto> result = apiTennisClient.fetchLivescores();

        assertThat(result).hasSize(1);
        assertEquals("8888", result.getFirst().eventKey());
        assertEquals("1", result.getFirst().eventLive());
    }

    @Test
    void shouldReturnEmptyListWhenNoLivescores() {
        stubFor(get(urlPathEqualTo("/tennis"))
                .withQueryParam("method", equalTo("get_livescore"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("wiremock/empty_result.json")));

        List<ApiMatchDto> result = apiTennisClient.fetchLivescores();

        assertThat(result).isEmpty();
    }

    // fetchStandings
    @Test
    void shouldFetchStandingsSuccessfully() {
        stubFor(get(urlPathEqualTo("/tennis"))
                .withQueryParam("method", equalTo("get_standings"))
                .withQueryParam("event_type", equalTo("ATP"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("wiremock/standings_success.json")));

        List<ApiStandingDto> result = apiTennisClient.fetchStandings("ATP");

        assertThat(result).hasSize(1);
        assertEquals("Novak Djokovic", result.getFirst().player());
        assertEquals("100", result.getFirst().playerKey());
        assertEquals("10000", result.getFirst().points());
        assertEquals("1", result.getFirst().place());
    }

    @Test
    void shouldThrowWhenStandingsResultIsEmpty() {
        stubFor(get(urlPathEqualTo("/tennis"))
                .withQueryParam("method", equalTo("get_standings"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("wiremock/empty_result.json")));

        assertThatThrownBy(() -> apiTennisClient.fetchStandings("ATP"))
                .isInstanceOf(IllegalStateException.class);
    }

    // fetchPlayer
    @Test
    void shouldFetchPlayerSuccessfully() {
        stubFor(get(urlPathEqualTo("/tennis"))
                .withQueryParam("method", equalTo("get_players"))
                .withQueryParam("player_key", equalTo("100"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("wiremock/player_success.json")));

        ApiPlayerDto result = apiTennisClient.fetchPlayer(100L);

        assertEquals("100", result.playerKey());
        assertEquals("Novak Djokovic", result.playerName());
        assertEquals("Serbia", result.playerCountry());
        assertEquals("22.05.1987", result.playerBday());
        assertEquals("https://example.com/djokovic.jpg", result.playerLogo());
    }

    @Test
    void shouldFetchPlayerWithStatsSuccessfully() {
        stubFor(get(urlPathEqualTo("/tennis"))
                .withQueryParam("method", equalTo("get_players"))
                .withQueryParam("player_key", equalTo("100"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("wiremock/player_with_stats_success.json")));

        ApiPlayerDto result = apiTennisClient.fetchPlayer(100L);

        assertThat(result.stats()).hasSize(1);
        assertEquals("2024", result.stats().getFirst().season());
        assertEquals("1", result.stats().getFirst().rank());
        assertEquals("7", result.stats().getFirst().titles());
    }

    @Test
    void shouldThrowWhenPlayerNotFound() {
        stubFor(get(urlPathEqualTo("/tennis"))
                .withQueryParam("method", equalTo("get_players"))
                .withQueryParam("player_key", equalTo("999"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("wiremock/empty_result.json")));

        assertThatThrownBy(() -> apiTennisClient.fetchPlayer(999L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldThrowWhenPlayerApiReturns500() {
        stubFor(get(urlPathEqualTo("/tennis"))
                .withQueryParam("method", equalTo("get_players"))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> apiTennisClient.fetchPlayer(100L))
                .isInstanceOf(Exception.class);
    }
}