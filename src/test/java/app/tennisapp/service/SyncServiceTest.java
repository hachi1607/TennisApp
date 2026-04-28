package app.tennisapp.service;

import app.tennisapp.client.ApiTennisClient;
import app.tennisapp.client.response.*;
import app.tennisapp.entity.*;
import app.tennisapp.mapper.*;
import app.tennisapp.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncServiceTest {
    @Mock
    private ApiTennisClient apiTennisClient;
    @Mock
    private TournamentRepository tournamentRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private PlayerSeasonStatsRepository playerSeasonStatsRepository;
    @Mock
    private MatchRepository matchRepository;
    @Mock
    private RankingRepository rankingRepository;
    @Mock
    private ApiTournamentMapper apiTournamentMapper;
    @Mock
    private ApiPlayerMapper apiPlayerMapper;
    @Mock
    private ApiRankingMapper apiRankingMapper;
    @Mock
    private ApiMatchMapper apiMatchMapper;
    @InjectMocks
    private SyncService syncService;

    private ApiTournamentDto buildApiTournamentDto(String key, String name, String type) {
        return new ApiTournamentDto(key, name, "1", type);
    }

    private ApiPlayerDto buildApiPlayerDto(String key, String name) {
        return new ApiPlayerDto(key, name, "Spain", "01.01.1990",
                "https://example.com/player.jpg", List.of());
    }

    private ApiPlayerDto buildApiPlayerDtoWithStats(String key, String name) {
        ApiPlayerStatsDto stats = new ApiPlayerStatsDto(
                "2024", "singles", "1", "5",
                "60", "10", "20", "3", "25", "4", "15", "3");
        return new ApiPlayerDto(key, name, "Spain", "01.01.1990",
                "https://example.com/player.jpg", List.of(stats));
    }

    private ApiStandingDto buildApiStandingDto(String playerKey, String player) {
        return new ApiStandingDto("1", player, playerKey, "ATP", "same", "Spain", "10000");
    }

    private ApiMatchDto buildApiMatchDto(String eventKey, String tournamentKey,
                                         String firstPlayerKey, String secondPlayerKey,
                                         String eventType, String date) {
        return new ApiMatchDto(eventKey, date, "14:00",
                "Novak Djokovic", firstPlayerKey,
                "Rafael Nadal", secondPlayerKey,
                "2-1", null, null, "First Player",
                "Finished", eventType, "Wimbledon",
                tournamentKey, "Final", "2025", "0", "False", List.of());
    }

    private Tournament buildTournament(Long id) {
        return Tournament.builder()
                .id(id)
                .externalId(1000L)
                .name("Wimbledon")
                .eventCategory(EventCategory.ATP_SINGLES)
                .build();
    }

    private Player buildPlayer(Long id, Long externalId) {
        return Player.builder()
                .id(id)
                .externalId(externalId)
                .fullName("Novak Djokovic")
                .build();
    }

    private Match buildMatch(Long id) {
        return Match.builder()
                .id(id)
                .externalId(9999L)
                .build();
    }

    // syncTournaments

    @Test
    void shouldSyncNewTournamentWhenNotExistsInDb() {
        ApiTournamentDto dto = buildApiTournamentDto("1000", "Wimbledon", "Atp Singles");
        Tournament tournament = buildTournament(1L);

        when(apiTennisClient.fetchTournaments()).thenReturn(List.of(dto));
        when(tournamentRepository.findByExternalId(1000L)).thenReturn(Optional.empty());
        when(apiTournamentMapper.toEntity(dto, 1000L, EventCategory.ATP_SINGLES)).thenReturn(tournament);

        syncService.syncTournaments();

        verify(apiTournamentMapper).toEntity(dto, 1000L, EventCategory.ATP_SINGLES);
        verify(apiTournamentMapper, never()).updateEntity(any(), any(), any());
        verify(tournamentRepository).save(tournament);
    }

    @Test
    void shouldUpdateExistingTournamentDuringSync() {
        ApiTournamentDto dto = buildApiTournamentDto("1000", "Wimbledon Updated", "Atp Singles");
        Tournament existing = buildTournament(1L);
        Tournament updated = buildTournament(1L);

        when(apiTennisClient.fetchTournaments()).thenReturn(List.of(dto));
        when(tournamentRepository.findByExternalId(1000L)).thenReturn(Optional.of(existing));
        when(apiTournamentMapper.updateEntity(existing, dto, EventCategory.ATP_SINGLES)).thenReturn(updated);

        syncService.syncTournaments();

        verify(apiTournamentMapper).updateEntity(existing, dto, EventCategory.ATP_SINGLES);
        verify(apiTournamentMapper, never()).toEntity(any(), any(), any());
        verify(tournamentRepository).save(updated);
    }

    @Test
    void shouldSkipTournamentWithUnknownCategory() {
        ApiTournamentDto dto = buildApiTournamentDto("1000", "Unknown Tournament", "Unknown Category");

        when(apiTennisClient.fetchTournaments()).thenReturn(List.of(dto));

        syncService.syncTournaments();

        verify(tournamentRepository, never()).save(any());
    }

    @Test
    void shouldSkipTournamentWithNullExternalId() {
        ApiTournamentDto dto = buildApiTournamentDto(null, "Wimbledon", "Atp Singles");

        when(apiTennisClient.fetchTournaments()).thenReturn(List.of(dto));

        syncService.syncTournaments();

        verify(tournamentRepository, never()).save(any());
    }

    // syncPlayer

    @Test
    void shouldCreateNewPlayerWhenNotExistsInDb() {
        ApiPlayerDto dto = buildApiPlayerDto("100", "Carlos Alcaraz");
        Player player = buildPlayer(1L, 100L);

        when(apiTennisClient.fetchPlayer(100L)).thenReturn(dto);
        when(playerRepository.findByExternalId(100L)).thenReturn(Optional.empty());
        when(apiPlayerMapper.toEntity(dto, 100L)).thenReturn(player);
        when(playerRepository.save(player)).thenReturn(player);

        syncService.syncPlayer(100L);

        verify(apiPlayerMapper).toEntity(dto, 100L);
        verify(apiPlayerMapper, never()).updateEntity(any(), any());
        verify(playerRepository).save(player);
    }

    @Test
    void shouldUpdateExistingPlayerDuringSync() {
        ApiPlayerDto dto = buildApiPlayerDto("100", "Carlos Alcaraz");
        Player existing = buildPlayer(1L, 100L);
        Player updated = buildPlayer(1L, 100L);

        when(apiTennisClient.fetchPlayer(100L)).thenReturn(dto);
        when(playerRepository.findByExternalId(100L)).thenReturn(Optional.of(existing));
        when(apiPlayerMapper.updateEntity(existing, dto)).thenReturn(updated);
        when(playerRepository.save(updated)).thenReturn(updated);

        syncService.syncPlayer(100L);

        verify(apiPlayerMapper).updateEntity(existing, dto);
        verify(apiPlayerMapper, never()).toEntity(any(), any());
    }

    @Test
    void shouldSyncPlayerStatsWhenStatsPresent() {
        ApiPlayerDto dto = buildApiPlayerDtoWithStats("100", "Carlos Alcaraz");
        Player player = buildPlayer(1L, 100L);
        PlayerSeasonStats stats = PlayerSeasonStats.builder().id(1L).player(player)
                .season("2024").type("singles").build();

        when(apiTennisClient.fetchPlayer(100L)).thenReturn(dto);
        when(playerRepository.findByExternalId(100L)).thenReturn(Optional.empty());
        when(apiPlayerMapper.toEntity(dto, 100L)).thenReturn(player);
        when(playerRepository.save(player)).thenReturn(player);
        when(playerSeasonStatsRepository.findByPlayerIdAndSeasonAndType(1L, "2024", "singles"))
                .thenReturn(Optional.empty());
        when(apiPlayerMapper.toStatsEntity(any(), eq(player))).thenReturn(stats);

        syncService.syncPlayer(100L);

        verify(playerSeasonStatsRepository).save(stats);
    }

    @Test
    void shouldSkipStatsWithBlankSeason() {
        ApiPlayerStatsDto invalidStats = new ApiPlayerStatsDto(
                "", "singles", "1", "5",
                "60", "10", "20", "3", "25", "4", "15", "3");
        ApiPlayerDto dto = new ApiPlayerDto("100", "Carlos Alcaraz", "Spain",
                "01.01.1990", null, List.of(invalidStats));
        Player player = buildPlayer(1L, 100L);

        when(apiTennisClient.fetchPlayer(100L)).thenReturn(dto);
        when(playerRepository.findByExternalId(100L)).thenReturn(Optional.empty());
        when(apiPlayerMapper.toEntity(dto, 100L)).thenReturn(player);
        when(playerRepository.save(player)).thenReturn(player);

        syncService.syncPlayer(100L);

        verify(playerSeasonStatsRepository, never()).save(any());
    }

    // syncStandings

    @Test
    void shouldSyncStandingsAndCreateNewPlayers() {
        ApiStandingDto dto = buildApiStandingDto("100", "Novak Djokovic");
        Player player = buildPlayer(1L, 100L);
        RankingEntry entry = RankingEntry.builder().id(1L).player(player)
                .rankingType(RankingType.ATP).build();

        when(apiTennisClient.fetchStandings("ATP")).thenReturn(List.of(dto));
        when(apiTennisClient.fetchStandings("WTA")).thenReturn(List.of());
        when(playerRepository.findByExternalId(100L)).thenReturn(Optional.empty());
        when(apiRankingMapper.toPlayerSkeleton(dto, 100L)).thenReturn(player);
        when(playerRepository.save(player)).thenReturn(player);
        when(apiRankingMapper.toEntity(eq(dto), eq(player), eq(RankingType.ATP), any()))
                .thenReturn(entry);

        syncService.syncStandings();

        verify(rankingRepository).deleteByRankingType(RankingType.ATP);
        verify(rankingRepository).deleteByRankingType(RankingType.WTA);
        verify(apiRankingMapper).toPlayerSkeleton(dto, 100L);
        verify(rankingRepository).save(entry);
    }

    @Test
    void shouldUseExistingPlayerDuringStandingsSync() {
        ApiStandingDto dto = buildApiStandingDto("100", "Novak Djokovic");
        Player existingPlayer = buildPlayer(1L, 100L);
        RankingEntry entry = RankingEntry.builder().id(1L).player(existingPlayer)
                .rankingType(RankingType.ATP).build();

        when(apiTennisClient.fetchStandings("ATP")).thenReturn(List.of(dto));
        when(apiTennisClient.fetchStandings("WTA")).thenReturn(List.of());
        when(playerRepository.findByExternalId(100L)).thenReturn(Optional.of(existingPlayer));
        when(apiRankingMapper.toEntity(eq(dto), eq(existingPlayer), eq(RankingType.ATP), any()))
                .thenReturn(entry);

        syncService.syncStandings();

        verify(apiRankingMapper, never()).toPlayerSkeleton(any(), any());
        verify(rankingRepository).save(entry);
    }

    @Test
    void shouldSkipStandingWithNullPlayerKey() {
        ApiStandingDto dto = new ApiStandingDto("1", "Unknown", null,
                "ATP", "same", "Spain", "1000");

        when(apiTennisClient.fetchStandings("ATP")).thenReturn(List.of(dto));
        when(apiTennisClient.fetchStandings("WTA")).thenReturn(List.of());

        syncService.syncStandings();

        verify(playerRepository, never()).save(any());
        verify(rankingRepository, never()).save(any());
    }

    // syncLivescores

    @Test
    void shouldResetLiveMatchesBeforeSyncingLivescores() {
        when(apiTennisClient.fetchLivescores()).thenReturn(List.of());

        syncService.syncLivescores();

        verify(matchRepository).resetAllLiveMatches();
    }

    @Test
    void shouldSyncLivescoresAndSaveMatches() {
        ApiMatchDto dto = buildApiMatchDto("9999", "1000", "1", "2",
                "Atp Singles", "2025-07-01");
        Tournament tournament = buildTournament(1L);
        Player firstPlayer = buildPlayer(1L, 1L);
        Player secondPlayer = buildPlayer(2L, 2L);
        Match match = buildMatch(1L);

        when(apiTennisClient.fetchLivescores()).thenReturn(List.of(dto));
        when(tournamentRepository.findByExternalId(1000L)).thenReturn(Optional.of(tournament));
        when(playerRepository.findByExternalId(1L)).thenReturn(Optional.of(firstPlayer));
        when(playerRepository.findByExternalId(2L)).thenReturn(Optional.of(secondPlayer));
        when(matchRepository.findByExternalId(9999L)).thenReturn(Optional.empty());
        when(apiMatchMapper.toEntity(eq(dto), eq(9999L), eq(firstPlayer), eq(secondPlayer), eq(tournament)))
                .thenReturn(match);

        syncService.syncLivescores();

        verify(matchRepository).resetAllLiveMatches();
        verify(matchRepository).save(match);
    }

    // syncFixtures

    @Test
    void shouldSyncFixturesAndSaveNewMatch() {
        LocalDate start = LocalDate.of(2025, 7, 1);
        LocalDate end = LocalDate.of(2025, 7, 7);
        ApiMatchDto dto = buildApiMatchDto("9999", "1000", "1", "2",
                "Atp Singles", "2025-07-01");
        Tournament tournament = buildTournament(1L);
        Player firstPlayer = buildPlayer(1L, 1L);
        Player secondPlayer = buildPlayer(2L, 2L);
        Match match = buildMatch(1L);

        when(apiTennisClient.fetchFixtures(start, end)).thenReturn(List.of(dto));
        when(tournamentRepository.findByExternalId(1000L)).thenReturn(Optional.of(tournament));
        when(playerRepository.findByExternalId(1L)).thenReturn(Optional.of(firstPlayer));
        when(playerRepository.findByExternalId(2L)).thenReturn(Optional.of(secondPlayer));
        when(matchRepository.findByExternalId(9999L)).thenReturn(Optional.empty());
        when(apiMatchMapper.toEntity(eq(dto), eq(9999L), eq(firstPlayer), eq(secondPlayer), eq(tournament)))
                .thenReturn(match);

        syncService.syncFixtures(start, end);

        verify(apiTennisClient).fetchFixtures(start, end);
        verify(matchRepository).save(match);
    }

    @Test
    void shouldUpdateExistingMatchDuringFixturesSync() {
        LocalDate start = LocalDate.of(2025, 7, 1);
        LocalDate end = LocalDate.of(2025, 7, 7);
        ApiMatchDto dto = buildApiMatchDto("9999", "1000", "1", "2",
                "Atp Singles", "2025-07-01");
        Tournament tournament = buildTournament(1L);
        Player firstPlayer = buildPlayer(1L, 1L);
        Player secondPlayer = buildPlayer(2L, 2L);
        Match existing = buildMatch(1L);
        Match updated = buildMatch(1L);

        when(apiTennisClient.fetchFixtures(start, end)).thenReturn(List.of(dto));
        when(tournamentRepository.findByExternalId(1000L)).thenReturn(Optional.of(tournament));
        when(playerRepository.findByExternalId(1L)).thenReturn(Optional.of(firstPlayer));
        when(playerRepository.findByExternalId(2L)).thenReturn(Optional.of(secondPlayer));
        when(matchRepository.findByExternalId(9999L)).thenReturn(Optional.of(existing));
        when(apiMatchMapper.updateEntity(eq(existing), eq(dto), eq(firstPlayer), eq(secondPlayer), eq(tournament)))
                .thenReturn(updated);

        syncService.syncFixtures(start, end);

        verify(apiMatchMapper).updateEntity(existing, dto, firstPlayer, secondPlayer, tournament);
        verify(apiMatchMapper, never()).toEntity(any(), any(), any(), any(), any());
        verify(matchRepository).save(updated);
    }

    @Test
    void shouldSkipMatchWithUnknownCategory() {
        LocalDate start = LocalDate.of(2025, 7, 1);
        LocalDate end = LocalDate.of(2025, 7, 7);
        ApiMatchDto dto = buildApiMatchDto("9999", "1000", "1", "2",
                "Unknown Category", "2025-07-01");

        when(apiTennisClient.fetchFixtures(start, end)).thenReturn(List.of(dto));

        syncService.syncFixtures(start, end);

        verify(matchRepository, never()).save(any());
    }

    @Test
    void shouldSkipMatchWithNullDate() {
        LocalDate start = LocalDate.of(2025, 7, 1);
        LocalDate end = LocalDate.of(2025, 7, 7);
        ApiMatchDto dto = buildApiMatchDto("9999", "1000", "1", "2",
                "Atp Singles", null);
        Tournament tournament = buildTournament(1L);
        Player firstPlayer = buildPlayer(1L, 1L);
        Player secondPlayer = buildPlayer(2L, 2L);

        when(apiTennisClient.fetchFixtures(start, end)).thenReturn(List.of(dto));
        when(tournamentRepository.findByExternalId(1000L)).thenReturn(Optional.of(tournament));
        when(playerRepository.findByExternalId(1L)).thenReturn(Optional.of(firstPlayer));
        when(playerRepository.findByExternalId(2L)).thenReturn(Optional.of(secondPlayer));

        syncService.syncFixtures(start, end);

        verify(matchRepository, never()).save(any());
    }

    @Test
    void shouldSkipMatchWithNullExternalIds() {
        LocalDate start = LocalDate.of(2025, 7, 1);
        LocalDate end = LocalDate.of(2025, 7, 7);
        ApiMatchDto dto = buildApiMatchDto(null, null, null, null,
                "Atp Singles", "2025-07-01");

        when(apiTennisClient.fetchFixtures(start, end)).thenReturn(List.of(dto));

        syncService.syncFixtures(start, end);

        verify(matchRepository, never()).save(any());
    }
}