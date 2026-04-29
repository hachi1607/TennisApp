package app.tennisapp.integrationtest;

import app.tennisapp.entity.EventCategory;
import app.tennisapp.entity.Tournament;
import app.tennisapp.repository.TournamentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TournamentIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TournamentRepository tournamentRepository;

    private Tournament savedTournament;

    @BeforeEach
    void setUp() {
        tournamentRepository.deleteAll();

        savedTournament = tournamentRepository.save(Tournament.builder()
                .externalId(1000L)
                .name("Wimbledon")
                .eventCategory(EventCategory.ATP_SINGLES)
                .build());
    }

    @AfterEach
    void tearDown() {
        tournamentRepository.deleteAll();
    }

    private void buildAndSaveTournament(Long externalId, String name, EventCategory category) {
        tournamentRepository.save(Tournament.builder()
                .externalId(externalId)
                .name(name)
                .eventCategory(category)
                .build());
    }

    // getTournaments
    @Test
    void shouldReturnAllTournamentsWhenNoFilters() throws Exception {
        mockMvc.perform(get("/api/v1/tournaments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Wimbledon")))
                .andExpect(jsonPath("$[0].eventCategory", is("ATP_SINGLES")))
                .andExpect(jsonPath("$[0].externalId", is(1000)));
    }

    @Test
    void shouldReturnEmptyListWhenNoTournaments() throws Exception {
        tournamentRepository.deleteAll();

        mockMvc.perform(get("/api/v1/tournaments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldFilterTournamentsByCategory() throws Exception {
        buildAndSaveTournament(2000L, "Roland Garros", EventCategory.ATP_SINGLES);
        buildAndSaveTournament(3000L, "US Open Women", EventCategory.WTA_SINGLES);

        mockMvc.perform(get("/api/v1/tournaments")
                        .param("category", "ATP_SINGLES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))) // wimbledon z setupa + roland garros
                .andExpect(jsonPath("$[*].eventCategory", everyItem(is("ATP_SINGLES"))));
    }

    @Test
    void shouldFilterTournamentsByName() throws Exception {
        buildAndSaveTournament(2000L, "Roland Garros", EventCategory.ATP_SINGLES);

        mockMvc.perform(get("/api/v1/tournaments")
                        .param("name", "wimbledon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Wimbledon")));
    }

    @Test
    void shouldFilterTournamentsByNameCaseInsensitive() throws Exception {
        buildAndSaveTournament(2000L, "Roland Garros", EventCategory.ATP_SINGLES);

        mockMvc.perform(get("/api/v1/tournaments")
                        .param("name", "WIMBLEDON"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Wimbledon")));
    }

    @Test
    void shouldFilterTournamentsByNamePartialMatch() throws Exception {
        buildAndSaveTournament(2000L, "Roland Garros", EventCategory.ATP_SINGLES);

        mockMvc.perform(get("/api/v1/tournaments")
                        .param("name", "mbled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Wimbledon")));
    }

    @Test
    void shouldFilterTournamentsByCategoryAndName() throws Exception {
        buildAndSaveTournament(2000L, "Wimbledon Women", EventCategory.WTA_SINGLES);

        mockMvc.perform(get("/api/v1/tournaments")
                        .param("category", "ATP_SINGLES")
                        .param("name", "wimbledon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Wimbledon")))
                .andExpect(jsonPath("$[0].eventCategory", is("ATP_SINGLES")));
    }

    @Test
    void shouldReturnEmptyListWhenNoTournamentsMatchFilters() throws Exception {
        mockMvc.perform(get("/api/v1/tournaments")
                        .param("name", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldReturn400WhenCategoryIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/tournaments")
                        .param("category", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    // getTournamentById
    @Test
    void shouldReturnTournamentByIdWhenFound() throws Exception {
        mockMvc.perform(get("/api/v1/tournaments/{id}", savedTournament.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedTournament.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Wimbledon")))
                .andExpect(jsonPath("$.eventCategory", is("ATP_SINGLES")))
                .andExpect(jsonPath("$.externalId", is(1000)));
    }

    @Test
    void shouldReturn404WhenTournamentNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/tournaments/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenTournamentIdIsInvalidType() throws Exception {
        mockMvc.perform(get("/api/v1/tournaments/{id}", "invalid"))
                .andExpect(status().isBadRequest());
    }
}