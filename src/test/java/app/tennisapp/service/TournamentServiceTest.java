package app.tennisapp.service;

import app.tennisapp.dto.TournamentDto;
import app.tennisapp.entity.EventCategory;
import app.tennisapp.entity.Tournament;
import app.tennisapp.exception.ResourceNotFoundException;
import app.tennisapp.mapper.TournamentMapper;
import app.tennisapp.repository.TournamentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TournamentServiceTest {
    @Mock
    private TournamentRepository tournamentRepository;
    @Mock
    private TournamentMapper tournamentMapper;
    @InjectMocks
    private TournamentService tournamentService;

    private Tournament buildTournament(Long id, EventCategory category) {
        return Tournament.builder()
                .id(id)
                .externalId(1000L)
                .name("Wimbledon")
                .eventCategory(category)
                .build();
    }

    private TournamentDto buildTournamentDto(Long id, EventCategory category) {
        return new TournamentDto(id, 1000L, "Wimbledon", category);
    }

    // getTournaments
    @Test
    void shouldReturnAllTournamentsWhenNoFilters() {
        Tournament tournament = buildTournament(1L, EventCategory.ATP_SINGLES);
        TournamentDto dto = buildTournamentDto(1L, EventCategory.ATP_SINGLES);

        when(tournamentRepository.findAll()).thenReturn(List.of(tournament));
        when(tournamentMapper.toDto(List.of(tournament))).thenReturn(List.of(dto));

        List<TournamentDto> result = tournamentService.getTournaments(null, null);

        assertNotNull(result);
        assertEquals(List.of(dto), result);
        verify(tournamentRepository).findAll();
        verify(tournamentRepository, never()).findByEventCategory(any());
        verify(tournamentRepository, never()).findByNameContainingIgnoreCase(any());
        verify(tournamentRepository, never()).findByEventCategoryAndNameContainingIgnoreCase(any(), any());
    }

    @Test
    void shouldFilterTournamentsByCategoryOnly() {
        Tournament tournament = buildTournament(1L, EventCategory.ATP_SINGLES);
        TournamentDto dto = buildTournamentDto(1L, EventCategory.ATP_SINGLES);

        when(tournamentRepository.findByEventCategory(EventCategory.ATP_SINGLES))
                .thenReturn(List.of(tournament));
        when(tournamentMapper.toDto(List.of(tournament))).thenReturn(List.of(dto));

        List<TournamentDto> result = tournamentService.getTournaments(EventCategory.ATP_SINGLES, null);

        assertNotNull(result);
        assertEquals(List.of(dto), result);
        verify(tournamentRepository).findByEventCategory(EventCategory.ATP_SINGLES);
        verify(tournamentRepository, never()).findAll();
        verify(tournamentRepository, never()).findByNameContainingIgnoreCase(any());
        verify(tournamentRepository, never()).findByEventCategoryAndNameContainingIgnoreCase(any(), any());
    }

    @Test
    void shouldFilterTournamentsByNameOnly() {
        Tournament tournament = buildTournament(1L, EventCategory.ATP_SINGLES);
        TournamentDto dto = buildTournamentDto(1L, EventCategory.ATP_SINGLES);

        when(tournamentRepository.findByNameContainingIgnoreCase("wimbledon"))
                .thenReturn(List.of(tournament));
        when(tournamentMapper.toDto(List.of(tournament))).thenReturn(List.of(dto));

        List<TournamentDto> result = tournamentService.getTournaments(null, "wimbledon");

        assertNotNull(result);
        assertEquals(List.of(dto), result);
        verify(tournamentRepository).findByNameContainingIgnoreCase("wimbledon");
        verify(tournamentRepository, never()).findAll();
        verify(tournamentRepository, never()).findByEventCategory(any());
        verify(tournamentRepository, never()).findByEventCategoryAndNameContainingIgnoreCase(any(), any());
    }

    @Test
    void shouldFilterTournamentsByCategoryAndName() {
        Tournament tournament = buildTournament(1L, EventCategory.ATP_SINGLES);
        TournamentDto dto = buildTournamentDto(1L, EventCategory.ATP_SINGLES);

        when(tournamentRepository.findByEventCategoryAndNameContainingIgnoreCase(
                EventCategory.ATP_SINGLES, "wimbledon"))
                .thenReturn(List.of(tournament));
        when(tournamentMapper.toDto(List.of(tournament))).thenReturn(List.of(dto));

        List<TournamentDto> result = tournamentService.getTournaments(EventCategory.ATP_SINGLES, "wimbledon");

        assertNotNull(result);
        assertEquals(List.of(dto), result);
        verify(tournamentRepository).findByEventCategoryAndNameContainingIgnoreCase(
                EventCategory.ATP_SINGLES, "wimbledon");
        verify(tournamentRepository, never()).findAll();
        verify(tournamentRepository, never()).findByEventCategory(any());
        verify(tournamentRepository, never()).findByNameContainingIgnoreCase(any());
    }

    @Test
    void shouldReturnEmptyListWhenNoTournamentsMatchFilters() {
        when(tournamentRepository.findByNameContainingIgnoreCase("xyz")).thenReturn(List.of());
        when(tournamentMapper.toDto(List.of())).thenReturn(List.of());

        List<TournamentDto> result = tournamentService.getTournaments(null, "xyz");

        assertNotNull(result);
        assertEquals(List.of(), result);
    }

    // getTournamentById
    @Test
    void shouldReturnTournamentWhenFound() {
        Tournament tournament = buildTournament(1L, EventCategory.ATP_SINGLES);
        TournamentDto dto = buildTournamentDto(1L, EventCategory.ATP_SINGLES);

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(tournamentMapper.toDto(tournament)).thenReturn(dto);

        TournamentDto result = tournamentService.getTournamentById(1L);

        assertNotNull(result);
        assertEquals(dto, result);
        verify(tournamentRepository).findById(1L);
        verify(tournamentMapper).toDto(tournament);
    }

    @Test
    void shouldThrowExceptionWhenTournamentNotFound() {
        when(tournamentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tournamentService.getTournamentById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(tournamentMapper, never()).toDto(any(Tournament.class));
    }
}