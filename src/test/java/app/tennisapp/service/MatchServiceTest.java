package app.tennisapp.service;

import app.tennisapp.dto.MatchDto;
import app.tennisapp.entity.Match;
import app.tennisapp.entity.Player;
import app.tennisapp.entity.Tournament;
import app.tennisapp.exception.ResourceNotFoundException;
import app.tennisapp.mapper.MatchMapper;
import app.tennisapp.dto.MatchFilterParams;
import app.tennisapp.repository.MatchRepository;
import app.tennisapp.specificator.MatchSpecs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {
    @Mock
    private MatchRepository matchRepository;
    @Mock
    private MatchMapper matchMapper;
    @Mock
    private MatchSpecs matchSpecs;
    @InjectMocks
    private MatchService matchService;

    private static final Specification<Match> EMPTY_SPEC = (root, query, cb) -> cb.conjunction();

    private Match buildMatch() {
        return Match.builder()
                .id(1L)
                .firstPlayer(Player.builder().id(1L).fullName("Novak Djokovic").build())
                .secondPlayer(Player.builder().id(2L).fullName("Rafael Nadal").build())
                .tournament(Tournament.builder().id(1L).name("Wimbledon").build())
                .date(LocalDate.of(2025, 7, 1))
                .isLive(false)
                .build();
    }

    private MatchDto buildMatchDto() {
        return new MatchDto(1L, 1000L, 1L, "Novak Djokovic", 2L, "Rafael Nadal",
                1L, "Wimbledon", "2025", LocalDate.of(2025, 7, 1),
                "14:00", "2-1", null, null, "First Player",
                "Finished", "Final", false, false, null);
    }

    // getMatches
    @Test
    void shouldReturnAllMatchesWithNoFilters() {
        JpaSpecificationExecutor<Match> specExecutor = matchRepository;
        Pageable pageable = PageRequest.of(0, 20);
        Match match = buildMatch();
        MatchDto dto = buildMatchDto();
        Page<Match> matchPage = new PageImpl<>(List.of(match), pageable, 1);
        Page<MatchDto> matchDtoPage = new PageImpl<>(List.of(dto), pageable, 1);

        when(matchSpecs.isLive(null)).thenReturn(EMPTY_SPEC);
        when(matchSpecs.hasPlayer(null)).thenReturn(EMPTY_SPEC);
        when(matchSpecs.headToHead(null, null)).thenReturn(EMPTY_SPEC);
        when(matchSpecs.hasTournament(null)).thenReturn(EMPTY_SPEC);
        when(matchSpecs.dateAfter(null)).thenReturn(EMPTY_SPEC);
        when(matchSpecs.dateBefore(null)).thenReturn(EMPTY_SPEC);
        when(specExecutor.findAll(any(), eq(pageable))).thenReturn(matchPage);
        when(matchMapper.toDto(match)).thenReturn(dto);

        MatchFilterParams params = new MatchFilterParams(null, null, null, null, null, null, null);
        Page<MatchDto> result = matchService.getMatches(params, pageable);

        assertNotNull(result);
        assertEquals(matchDtoPage, result);
        verify(specExecutor).findAll(any(), eq(pageable));
    }

    @Test
    void shouldPassSpecsToRepository() {
        JpaSpecificationExecutor<Match> specExecutor = matchRepository;
        Pageable pageable = PageRequest.of(0, 20);
        LocalDate dateFrom = LocalDate.of(2025, 1, 1);
        LocalDate dateTo = LocalDate.of(2025, 12, 31);

        when(matchSpecs.isLive(true)).thenReturn(EMPTY_SPEC);
        when(matchSpecs.hasPlayer(1L)).thenReturn(EMPTY_SPEC);
        when(matchSpecs.headToHead(1L, 2L)).thenReturn(EMPTY_SPEC);
        when(matchSpecs.hasTournament(1L)).thenReturn(EMPTY_SPEC);
        when(matchSpecs.dateAfter(dateFrom)).thenReturn(EMPTY_SPEC);
        when(matchSpecs.dateBefore(dateTo)).thenReturn(EMPTY_SPEC);
        when(specExecutor.findAll(any(), eq(pageable))).thenReturn(Page.empty());

        MatchFilterParams params = new MatchFilterParams(true, 1L, 1L, 2L, 1L, dateFrom, dateTo);
        matchService.getMatches(params, pageable);

        verify(matchSpecs).isLive(true);
        verify(matchSpecs).hasPlayer(1L);
        verify(matchSpecs).headToHead(1L, 2L);
        verify(matchSpecs).hasTournament(1L);
        verify(matchSpecs).dateAfter(dateFrom);
        verify(matchSpecs).dateBefore(dateTo);
        verify(specExecutor).findAll(any(), eq(pageable));
    }

    @Test
    void shouldReturnEmptyPageWhenEmpty() {
        JpaSpecificationExecutor<Match> specExecutor = matchRepository;
        Pageable pageable = PageRequest.of(0, 20);

        when(matchSpecs.isLive(null)).thenReturn(EMPTY_SPEC);
        when(matchSpecs.hasPlayer(null)).thenReturn(EMPTY_SPEC);
        when(matchSpecs.headToHead(null, null)).thenReturn(EMPTY_SPEC);
        when(matchSpecs.hasTournament(null)).thenReturn(EMPTY_SPEC);
        when(matchSpecs.dateAfter(null)).thenReturn(EMPTY_SPEC);
        when(matchSpecs.dateBefore(null)).thenReturn(EMPTY_SPEC);
        when(specExecutor.findAll(any(), eq(pageable))).thenReturn(Page.empty());

        MatchFilterParams params = new MatchFilterParams(null, null, null, null, null, null, null);
        Page<MatchDto> result = matchService.getMatches(params, pageable);

        assertThat(result.getContent()).isEmpty();
    }

    // getMatchById
    @Test
    void shouldReturnDtoWhenExists() {
        Match match = buildMatch();
        MatchDto dto = buildMatchDto();

        when(matchRepository.getMatchByIdWithPlayers(1L)).thenReturn(Optional.of(match));
        when(matchMapper.toDto(match)).thenReturn(dto);

        MatchDto result = matchService.getMatchById(1L);

        assertNotNull(result);
        assertEquals(dto, result);
        verify(matchRepository, times(1)).getMatchByIdWithPlayers(1L);
        verify(matchMapper, times(1)).toDto(match);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenNotExists() {
        when(matchRepository.getMatchByIdWithPlayers(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.getMatchById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(matchMapper, never()).toDto(any(Match.class));
    }
}