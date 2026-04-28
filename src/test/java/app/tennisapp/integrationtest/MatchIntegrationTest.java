package app.tennisapp.integrationtest;

import app.tennisapp.entity.*;
import app.tennisapp.repository.*;
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
class MatchIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private PlayerRepository playerRepository;

    private Tournament savedTournament;
    private Player savedFirstPlayer;
    private Player savedSecondPlayer;
    private Match savedMatch;

    @BeforeEach
    void setUp() {
        matchRepository.deleteAll();
        playerRepository.deleteAll();
        tournamentRepository.deleteAll();

        savedTournament = tournamentRepository.save(Tournament.builder()
                .externalId(1000L)
                .name("Wimbledon")
                .eventCategory(EventCategory.ATP_SINGLES)
                .build());

        savedFirstPlayer = playerRepository.save(Player.builder()
                .externalId(1L)
                .fullName("Novak Djokovic")
                .nationality("Serbia")
                .build());

        savedSecondPlayer = playerRepository.save(Player.builder()
                .externalId(2L)
                .fullName("Rafael Nadal")
                .nationality("Spain")
                .build());

        savedMatch = matchRepository.save(Match.builder()
                .externalId(9999L)
                .firstPlayer(savedFirstPlayer)
                .secondPlayer(savedSecondPlayer)
                .tournament(savedTournament)
                .season("2025")
                .date(LocalDate.of(2025, 7, 1))
                .time("14:00")
                .finalResult("2-1")
                .winner("Novak Djokovic")
                .status("Finished")
                .round("Final")
                .isLive(false)
                .qualification(false)
                .build());
    }

    @AfterEach
    void tearDown() {
        matchRepository.deleteAll();
        playerRepository.deleteAll();
        tournamentRepository.deleteAll();
    }

    private void buildAndSaveMatch(Long externalId, Player first, Player second,
                                   Tournament tournament, LocalDate date, boolean isLive) {
        matchRepository.save(Match.builder()
                .externalId(externalId)
                .firstPlayer(first)
                .secondPlayer(second)
                .tournament(tournament)
                .season("2025")
                .date(date)
                .isLive(isLive)
                .qualification(false)
                .build());
    }

    // getMatches
    @Test
    void shouldReturnPagedMatchesWithNoFilters() throws Exception {
        mockMvc.perform(get("/api/v1/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].externalId", is(9999)))
                .andExpect(jsonPath("$.content[0].firstPlayerName", is("Novak Djokovic")))
                .andExpect(jsonPath("$.content[0].secondPlayerName", is("Rafael Nadal")))
                .andExpect(jsonPath("$.content[0].tournamentName", is("Wimbledon")));
    }

    @Test
    void shouldReturnEmptyPageWhenNoMatches() throws Exception {
        matchRepository.deleteAll();

        mockMvc.perform(get("/api/v1/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void shouldFilterMatchesByLive() throws Exception {
        buildAndSaveMatch(8888L, savedFirstPlayer, savedSecondPlayer,
                savedTournament, LocalDate.of(2025, 7, 2), true);

        mockMvc.perform(get("/api/v1/matches")
                        .param("live", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].externalId", is(8888)))
                .andExpect(jsonPath("$.content[0].isLive", is(true)));
    }

    @Test
    void shouldFilterMatchesByPlayerId() throws Exception {
        Player thirdPlayer = playerRepository.save(Player.builder()
                .externalId(3L)
                .fullName("Carlos Alcaraz")
                .nationality("Spain")
                .build());

        buildAndSaveMatch(8888L, thirdPlayer, savedSecondPlayer,
                savedTournament, LocalDate.of(2025, 7, 2), false);

        mockMvc.perform(get("/api/v1/matches")
                        .param("playerId", savedFirstPlayer.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].externalId", is(9999)));
    }

    @Test
    void shouldFilterMatchesByTournamentId() throws Exception {
        Tournament otherTournament = tournamentRepository.save(Tournament.builder()
                .externalId(2000L)
                .name("Roland Garros")
                .eventCategory(EventCategory.ATP_SINGLES)
                .build());

        buildAndSaveMatch(8888L, savedFirstPlayer, savedSecondPlayer,
                otherTournament, LocalDate.of(2025, 6, 1), false);

        mockMvc.perform(get("/api/v1/matches")
                        .param("tournamentId", savedTournament.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].tournamentName", is("Wimbledon")));
    }

    @Test
    void shouldFilterMatchesByDateFrom() throws Exception {
        buildAndSaveMatch(8888L, savedFirstPlayer, savedSecondPlayer,
                savedTournament, LocalDate.of(2025, 6, 1), false);

        mockMvc.perform(get("/api/v1/matches")
                        .param("dateFrom", "2025-07-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].externalId", is(9999)));
    }

    @Test
    void shouldFilterMatchesByDateTo() throws Exception {
        buildAndSaveMatch(8888L, savedFirstPlayer, savedSecondPlayer,
                savedTournament, LocalDate.of(2025, 8, 1), false);

        mockMvc.perform(get("/api/v1/matches")
                        .param("dateTo", "2025-07-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].externalId", is(9999)));
    }

    @Test
    void shouldFilterMatchesByHeadToHead() throws Exception {
        Player thirdPlayer = playerRepository.save(Player.builder()
                .externalId(3L)
                .fullName("Carlos Alcaraz")
                .nationality("Spain")
                .build());

        buildAndSaveMatch(8888L, savedFirstPlayer, thirdPlayer,
                savedTournament, LocalDate.of(2025, 7, 2), false);

        mockMvc.perform(get("/api/v1/matches")
                        .param("firstPlayerId", savedFirstPlayer.getId().toString())
                        .param("secondPlayerId", savedSecondPlayer.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].externalId", is(9999)));
    }

    @Test
    void shouldReturnMatchesReverseOrderedByDateByDefault() throws Exception {
        buildAndSaveMatch(8888L, savedFirstPlayer, savedSecondPlayer,
                savedTournament, LocalDate.of(2025, 7, 10), false);

        mockMvc.perform(get("/api/v1/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].externalId", is(8888)))
                .andExpect(jsonPath("$.content[1].externalId", is(9999)));
    }

    // getMatchById
    @Test
    void shouldReturnMatchWhenFound() throws Exception {
        mockMvc.perform(get("/api/v1/matches/{id}", savedMatch.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedMatch.getId().intValue())))
                .andExpect(jsonPath("$.externalId", is(9999)))
                .andExpect(jsonPath("$.firstPlayerName", is("Novak Djokovic")))
                .andExpect(jsonPath("$.secondPlayerName", is("Rafael Nadal")))
                .andExpect(jsonPath("$.tournamentName", is("Wimbledon")))
                .andExpect(jsonPath("$.season", is("2025")))
                .andExpect(jsonPath("$.date", is("2025-07-01")));
    }

    @Test
    void shouldReturn404WhenMatchNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/matches/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenMatchIdIsInvalidType() throws Exception {
        mockMvc.perform(get("/api/v1/matches/{id}", "invalid"))
                .andExpect(status().isBadRequest());
    }
}