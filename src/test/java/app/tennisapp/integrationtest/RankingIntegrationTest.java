package app.tennisapp.integrationtest;

import app.tennisapp.entity.Player;
import app.tennisapp.entity.RankingEntry;
import app.tennisapp.entity.RankingType;
import app.tennisapp.repository.PlayerRepository;
import app.tennisapp.repository.RankingRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RankingIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RankingRepository rankingRepository;
    @Autowired
    private PlayerRepository playerRepository;

    private Player savedPlayer;

    @BeforeEach
    void setUp() {
        rankingRepository.deleteAll();
        playerRepository.deleteAll();

        savedPlayer = playerRepository.save(Player.builder()
                .externalId(100L)
                .fullName("Novak Djokovic")
                .nationality("Serbia")
                .build());
    }

    @AfterEach
    void tearDown() {
        rankingRepository.deleteAll();
        playerRepository.deleteAll();
    }

    private void buildAndSaveEntry(Player player, int position, int points, RankingType type) {
        rankingRepository.save(RankingEntry.builder()
                .player(player)
                .position(position)
                .points(points)
                .movement("same")
                .rankingType(type)
                .rankingDate(LocalDate.now())
                .build());
    }

    // getRankingByType
    @Test
    void shouldReturnAtpRanking() throws Exception {
        buildAndSaveEntry(savedPlayer, 1, 10000, RankingType.ATP);

        mockMvc.perform(get("/api/v1/rankings/{type}", "ATP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].position", is(1)))
                .andExpect(jsonPath("$[0].points", is(10000)))
                .andExpect(jsonPath("$[0].playerName", is("Novak Djokovic")))
                .andExpect(jsonPath("$[0].nationality", is("Serbia")))
                .andExpect(jsonPath("$[0].rankingType", is("ATP")));
    }

    @Test
    void shouldReturnWtaRanking() throws Exception {
        Player wtaPlayer = playerRepository.save(Player.builder()
                .externalId(200L)
                .fullName("Iga Swiatek")
                .nationality("Poland")
                .build());

        buildAndSaveEntry(wtaPlayer, 1, 11000, RankingType.WTA);

        mockMvc.perform(get("/api/v1/rankings/{type}", "WTA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].playerName", is("Iga Swiatek")))
                .andExpect(jsonPath("$[0].rankingType", is("WTA")));
    }

    @Test
    void shouldReturnRankingOrderedByPositionAsc() throws Exception {
        Player secondPlayer = playerRepository.save(Player.builder()
                .externalId(200L)
                .fullName("Carlos Alcaraz")
                .nationality("Spain")
                .build());

        buildAndSaveEntry(secondPlayer, 2, 9000, RankingType.ATP);
        buildAndSaveEntry(savedPlayer, 1, 10000, RankingType.ATP);

        mockMvc.perform(get("/api/v1/rankings/{type}", "ATP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].position", is(1)))
                .andExpect(jsonPath("$[0].playerName", is("Novak Djokovic")))
                .andExpect(jsonPath("$[1].position", is(2)))
                .andExpect(jsonPath("$[1].playerName", is("Carlos Alcaraz")));
    }

    @Test
    void shouldReturnOnlyAtpEntriesWhenRequestingAtp() throws Exception {
        Player wtaPlayer = playerRepository.save(Player.builder()
                .externalId(200L)
                .fullName("Iga Swiatek")
                .nationality("Poland")
                .build());

        buildAndSaveEntry(savedPlayer, 1, 10000, RankingType.ATP);
        buildAndSaveEntry(wtaPlayer, 1, 11000, RankingType.WTA);

        mockMvc.perform(get("/api/v1/rankings/{type}", "ATP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].rankingType", is("ATP")));
    }

    @Test
    void shouldReturnEmptyListWhenNoEntriesForType() throws Exception {
        mockMvc.perform(get("/api/v1/rankings/{type}", "ATP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldReturn400WhenRankingTypeIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/rankings/{type}", "INVALID"))
                .andExpect(status().isBadRequest());
    }
}