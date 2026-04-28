package app.tennisapp.integrationtest;

import app.tennisapp.client.ApiTennisClient;
import app.tennisapp.client.response.ApiPlayerDto;
import app.tennisapp.entity.Player;
import app.tennisapp.entity.PlayerSeasonStats;
import app.tennisapp.repository.PlayerRepository;
import app.tennisapp.repository.PlayerSeasonStatsRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PlayerIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private PlayerSeasonStatsRepository playerSeasonStatsRepository;
    @MockitoBean
    private ApiTennisClient apiTennisClient;

    private Player savedPlayer;

    @BeforeEach
    void setUp() {
        playerSeasonStatsRepository.deleteAll();
        playerRepository.deleteAll();

        savedPlayer = playerRepository.save(Player.builder()
                .externalId(100L)
                .fullName("Carlos Alcaraz")
                .nationality("Spain")
                .birthDate(LocalDate.of(2003, 5, 5))
                .imageUrl("https://example.com/alcaraz.jpg")
                .lastSyncedAt(LocalDateTime.now())
                .build());
    }

    @AfterEach
    void tearDown() {
        playerSeasonStatsRepository.deleteAll();
        playerRepository.deleteAll();
    }

    private Player buildAndSavePlayer(Long externalId, String fullName, String nationality) {
        return playerRepository.save(Player.builder()
                .externalId(externalId)
                .fullName(fullName)
                .nationality(nationality)
                .lastSyncedAt(LocalDateTime.now())
                .build());
    }

    private PlayerSeasonStats buildAndSaveStats(Player player, String season, String type) {
        return playerSeasonStatsRepository.save(PlayerSeasonStats.builder()
                .player(player)
                .season(season)
                .type(type)
                .rank(1)
                .titles(5)
                .matchesWon(60)
                .matchesLost(10)
                .hardWon(20)
                .hardLost(3)
                .clayWon(25)
                .clayLost(4)
                .grassWon(15)
                .grassLost(3)
                .build());
    }

    // getPlayers
    @Test
    void shouldReturnAllPlayersPagedWhenNoFilters() throws Exception {
        mockMvc.perform(get("/api/v1/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].fullName", is("Carlos Alcaraz")))
                .andExpect(jsonPath("$.content[0].nationality", is("Spain")))
                .andExpect(jsonPath("$.content[0].externalId", is(100)));
    }

    @Test
    void shouldReturnEmptyPageWhenNoPlayers() throws Exception {
        playerRepository.deleteAll();

        mockMvc.perform(get("/api/v1/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void shouldFilterPlayersByName() throws Exception {
        buildAndSavePlayer(200L, "Rafael Nadal", "Spain");

        mockMvc.perform(get("/api/v1/players")
                        .param("name", "alcaraz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].fullName", is("Carlos Alcaraz")));
    }

    @Test
    void shouldFilterPlayersByNameCaseInsensitive() throws Exception {
        buildAndSavePlayer(200L, "Rafael Nadal", "Spain");

        mockMvc.perform(get("/api/v1/players")
                        .param("name", "ALCARAZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].fullName", is("Carlos Alcaraz")));
    }

    @Test
    void shouldFilterPlayersByNationality() throws Exception {
        buildAndSavePlayer(200L, "Novak Djokovic", "Serbia");

        mockMvc.perform(get("/api/v1/players")
                        .param("nationality", "Spain"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].fullName", is("Carlos Alcaraz")));
    }

    @Test
    void shouldFilterPlayersByNameAndNationality() throws Exception {
        buildAndSavePlayer(200L, "Rafael Nadal", "Spain");

        mockMvc.perform(get("/api/v1/players")
                        .param("name", "alcaraz")
                        .param("nationality", "Spain"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].fullName", is("Carlos Alcaraz")));
    }

    @Test
    void shouldReturnEmptyPageWhenNoPlayersMatchFilters() throws Exception {
        mockMvc.perform(get("/api/v1/players")
                        .param("name", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void shouldReturnPlayersOrderedByFullNameAscByDefault() throws Exception {
        buildAndSavePlayer(200L, "Rafael Nadal", "Spain");

        mockMvc.perform(get("/api/v1/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].fullName", is("Carlos Alcaraz")))
                .andExpect(jsonPath("$.content[1].fullName", is("Rafael Nadal")));
    }

    // getPlayerById
    @Test
    void shouldReturnPlayerByIdWhenFound() throws Exception {
        mockMvc.perform(get("/api/v1/players/{id}", savedPlayer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedPlayer.getId().intValue())))
                .andExpect(jsonPath("$.fullName", is("Carlos Alcaraz")))
                .andExpect(jsonPath("$.nationality", is("Spain")))
                .andExpect(jsonPath("$.birthDate", is("2003-05-05")))
                .andExpect(jsonPath("$.imageUrl", is("https://example.com/alcaraz.jpg")));
    }

    @Test
    void shouldTriggerSyncWhenPlayerNotSynced() throws Exception {
        Player unsyncedPlayer = playerRepository.save(Player.builder()
                .externalId(999L)
                .fullName("Jannik Sinner")
                .nationality("Italy")
                .lastSyncedAt(null)
                .build());
        // wywoływane tylko, gdy są do uzupełnienia dane (czyli kiedy nie było synchronizacji jeszcze)
        when(apiTennisClient.fetchPlayer(999L)).thenReturn(
                new ApiPlayerDto("999", "Jannik Sinner", "Italy", null, null, List.of())
        );

        mockMvc.perform(get("/api/v1/players/{id}", unsyncedPlayer.getId()))
                .andExpect(status().isOk());

        verify(apiTennisClient, times(1)).fetchPlayer(999L);
    }

    @Test
    void shouldReturn404WhenPlayerNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/players/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenPlayerIdIsInvalidType() throws Exception {
        mockMvc.perform(get("/api/v1/players/{id}", "invalid"))
                .andExpect(status().isBadRequest());
    }

    // getPlayerStats
    @Test
    void shouldReturnPlayerStats() throws Exception {
        buildAndSaveStats(savedPlayer, "2024", "singles");

        mockMvc.perform(get("/api/v1/players/{id}/stats", savedPlayer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].season", is("2024")))
                .andExpect(jsonPath("$[0].type", is("singles")))
                .andExpect(jsonPath("$[0].rank", is(1)))
                .andExpect(jsonPath("$[0].titles", is(5)))
                .andExpect(jsonPath("$[0].matchesWon", is(60)))
                .andExpect(jsonPath("$[0].matchesLost", is(10)));
    }

    @Test
    void shouldReturnMultipleSeasonStats() throws Exception {
        buildAndSaveStats(savedPlayer, "2024", "singles");
        buildAndSaveStats(savedPlayer, "2023", "singles");

        mockMvc.perform(get("/api/v1/players/{id}/stats", savedPlayer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldReturnEmptyStatsListWhenPlayerHasNoStats() throws Exception {
        mockMvc.perform(get("/api/v1/players/{id}/stats", savedPlayer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldReturn404WhenPlayerNotFoundOnStats() throws Exception {
        mockMvc.perform(get("/api/v1/players/{id}/stats", 999L))
                .andExpect(status().isNotFound());
    }
}