package app.tennisapp.service;

import app.tennisapp.config.SyncConfig;
import app.tennisapp.dto.PlayerDto;
import app.tennisapp.dto.PlayerSeasonStatsDto;
import app.tennisapp.entity.Player;
import app.tennisapp.entity.PlayerSeasonStats;
import app.tennisapp.exception.ResourceNotFoundException;
import app.tennisapp.mapper.PlayerMapper;
import app.tennisapp.repository.PlayerRepository;
import app.tennisapp.repository.PlayerSeasonStatsRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {
    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerSeasonStatsRepository playerSeasonStatsRepository;

    @Mock
    private PlayerMapper playerMapper;

    @Mock
    private SyncService syncService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private SyncConfig syncConfig;

    @InjectMocks
    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        lenient().when(syncConfig.getPlayerTtlHours()).thenReturn(24L);
    }

    private Player samplePlayer() {
        return Player.builder()
                .id(1L)
                .externalId(100L)
                .fullName("Carlos Alcaraz")
                .nationality("ESP")
                .birthDate(LocalDate.of(2003, 5, 5))
                .imageUrl("https://example.com/alcaraz.jpg")
                .lastSyncedAt(LocalDateTime.now())
                .build();
    }

    private PlayerDto samplePlayerDto() {
        return new PlayerDto(1L, 100L, "Carlos Alcaraz", "ESP",
                LocalDate.of(2003, 5, 5), null, "https://example.com/alcaraz.jpg");
    }

    private PlayerSeasonStats sampleSeasonStats(Player player) {
        return PlayerSeasonStats.builder()
                .id(1L)
                .player(player)
                .season("2024")
                .type("singles")
                .rank(3)
                .titles(5)
                .matchesWon(60)
                .matchesLost(10)
                .hardWon(20)
                .hardLost(3)
                .clayWon(25)
                .clayLost(4)
                .grassWon(15)
                .grassLost(3)
                .build();
    }

    private PlayerSeasonStatsDto sampleSeasonStatsDto() {
        return new PlayerSeasonStatsDto(1L, 1L, "2024", "singles",
                3, 5, 60, 10, 20, 3, 25, 4, 15, 3);
    }


    // getPlayers
    @Test
    void shouldReturnAllPlayersPagedWhenNoFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Player player = samplePlayer();
        Page<Player> playerPage = new PageImpl<>(List.of(player));

        when(playerRepository.findAllPaged(pageable)).thenReturn(playerPage);
        when(playerMapper.toDto(player)).thenReturn(samplePlayerDto());

        Page<PlayerDto> result = playerService.getPlayers(null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().fullName()).isEqualTo("Carlos Alcaraz");
        verify(playerRepository, times(1)).findAllPaged(pageable);
        verify(playerRepository, never()).findByFullNameContainingIgnoreCase(any(), any());
        verify(playerRepository, never()).findByNationalityIgnoreCase(any(), any());
        verify(playerRepository, never()).findByFullNameContainingIgnoreCaseAndNationalityIgnoreCase(any(), any(), any());
    }

    @Test
    void shouldFilterPlayersByNameOnly() {
        Pageable pageable = PageRequest.of(0, 10);
        Player player = samplePlayer();
        Page<Player> playerPage = new PageImpl<>(List.of(player));
        String name = "alcaraz";

        when(playerRepository.findByFullNameContainingIgnoreCase(name, pageable)).thenReturn(playerPage);
        when(playerMapper.toDto(player)).thenReturn(samplePlayerDto());

        Page<PlayerDto> result = playerService.getPlayers(name, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(playerRepository, times(1)).findByFullNameContainingIgnoreCase(name, pageable);
        verify(playerRepository, never()).findAllPaged(any());
        verify(playerRepository, never()).findByNationalityIgnoreCase(any(), any());
    }

    @Test
    void shouldFilterPlayersByNationalityOnly() {
        Pageable pageable = PageRequest.of(0, 10);
        Player player = samplePlayer();
        Page<Player> playerPage = new PageImpl<>(List.of(player));
        String nationality = "ESP";

        when(playerRepository.findByNationalityIgnoreCase(nationality, pageable)).thenReturn(playerPage);
        when(playerMapper.toDto(player)).thenReturn(samplePlayerDto());

        Page<PlayerDto> result = playerService.getPlayers(null, nationality, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().nationality()).isEqualTo("ESP");
        verify(playerRepository, times(1)).findByNationalityIgnoreCase(nationality, pageable);
        verify(playerRepository, never()).findAllPaged(any());
        verify(playerRepository, never()).findByFullNameContainingIgnoreCase(any(), any());
    }

    @Test
    void shouldFilterPlayersByNameAndNationality() {
        Pageable pageable = PageRequest.of(0, 10);
        Player player = samplePlayer();
        Page<Player> playerPage = new PageImpl<>(List.of(player));
        String name = "alcaraz";
        String nationality = "ESP";

        when(playerRepository.findByFullNameContainingIgnoreCaseAndNationalityIgnoreCase(name, nationality, pageable))
                .thenReturn(playerPage);
        when(playerMapper.toDto(player)).thenReturn(samplePlayerDto());

        Page<PlayerDto> result = playerService.getPlayers(name, nationality, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(playerRepository, times(1))
                .findByFullNameContainingIgnoreCaseAndNationalityIgnoreCase(name, nationality, pageable);
        verify(playerRepository, never()).findAllPaged(any());
        verify(playerRepository, never()).findByFullNameContainingIgnoreCase(any(), any());
        verify(playerRepository, never()).findByNationalityIgnoreCase(any(), any());
    }

    @Test
    void shouldReturnEmptyPageWhenNoPlayersMatchFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        String name = "nonexistent";

        when(playerRepository.findByFullNameContainingIgnoreCase(name, pageable)).thenReturn(Page.empty());

        Page<PlayerDto> result = playerService.getPlayers(name, null, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void shouldTriggerSyncWhenPlayerDataIsStale() {
        Player player = Player.builder()
                .id(1L)
                .externalId(100L)
                .fullName("Carlos Alcaraz")
                .nationality("ESP")
                .lastSyncedAt(LocalDateTime.now().minusHours(25)) // stare dane
                .build();
        PlayerDto playerDto = samplePlayerDto();

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(playerMapper.toDto(player)).thenReturn(playerDto);

        playerService.getPlayerById(1L);

        verify(syncService, times(1)).syncPlayer(100L);
        verify(entityManager, times(1)).refresh(player);
    }

    @Test
    void shouldTriggerSyncAndRefreshWhenPlayerNotSynced() {
        Player player = Player.builder()
                .id(1L)
                .externalId(100L)
                .fullName("Carlos Alcaraz")
                .nationality("ESP")
                .lastSyncedAt(null) // nie zsynchronizowany
                .build();
        PlayerDto playerDto = samplePlayerDto();

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(playerMapper.toDto(player)).thenReturn(playerDto);

        PlayerDto result = playerService.getPlayerById(1L);

        assertThat(result).isEqualTo(playerDto);
        verify(syncService, times(1)).syncPlayer(100L);
        verify(entityManager, times(1)).refresh(player);
    }

    @Test
    void shouldReturnPlayerByIdWhenAlreadySynced() {
        Player player = Player.builder()
                .id(1L)
                .externalId(100L)
                .fullName("Carlos Alcaraz")
                .nationality("ESP")
                .lastSyncedAt(LocalDateTime.now()) // świeże dane → brak synca
                .build();
        PlayerDto playerDto = samplePlayerDto();

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(playerMapper.toDto(player)).thenReturn(playerDto);

        PlayerDto result = playerService.getPlayerById(1L);

        assertNotNull(result);
        assertEquals(playerDto, result);
        verify(syncService, never()).syncPlayer(anyLong());
        verify(entityManager, never()).refresh(any());
    }

    @Test
    void shouldThrowExceptionWhenPlayerNotFound() {
        Long givenId = 99L;

        when(playerRepository.findById(givenId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.getPlayerById(givenId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Player not found: " + givenId);

        verify(syncService, never()).syncPlayer(anyLong());
        verify(playerMapper, never()).toDto(any(Player.class));
    }

    // getPlayerStats
    @Test
    void shouldReturnPlayerStats() {
        Player player = samplePlayer();
        List<PlayerSeasonStats> statsList = List.of(sampleSeasonStats(player));
        List<PlayerSeasonStatsDto> statsDtos = List.of(sampleSeasonStatsDto());
        Long playerId = 1L;

        when(playerRepository.existsById(playerId)).thenReturn(true);
        when(playerSeasonStatsRepository.findByPlayerId(playerId)).thenReturn(statsList);
        when(playerMapper.toStatsDto(statsList)).thenReturn(statsDtos);

        List<PlayerSeasonStatsDto> result = playerService.getPlayerStats(playerId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().season()).isEqualTo("2024");
        assertThat(result.getFirst().matchesWon()).isEqualTo(60);
        verify(playerRepository, times(1)).existsById(playerId);
        verify(playerSeasonStatsRepository, times(1)).findByPlayerId(playerId);
    }

    @Test
    void shouldThrowExceptionWhenGettingStatsForNonExistentPlayer() {
        Long playerId = 99L;

        when(playerRepository.existsById(playerId)).thenReturn(false);

        assertThatThrownBy(() -> playerService.getPlayerStats(playerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Player not found: " + playerId);

        verify(playerSeasonStatsRepository, never()).findByPlayerId(anyLong());
        verify(playerMapper, never()).toStatsDto(anyList());
    }

    @Test
    void shouldReturnEmptyStatsListWhenPlayerHasNoStats() {
        Long playerId = 1L;

        when(playerRepository.existsById(playerId)).thenReturn(true);
        when(playerSeasonStatsRepository.findByPlayerId(playerId)).thenReturn(List.of());
        when(playerMapper.toStatsDto(List.of())).thenReturn(List.of());

        List<PlayerSeasonStatsDto> result = playerService.getPlayerStats(playerId);

        assertThat(result).isEmpty();
        verify(playerSeasonStatsRepository, times(1)).findByPlayerId(playerId);
    }
}