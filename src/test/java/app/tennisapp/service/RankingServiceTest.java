package app.tennisapp.service;

import app.tennisapp.dto.RankingEntryDto;
import app.tennisapp.entity.Player;
import app.tennisapp.entity.RankingEntry;
import app.tennisapp.entity.RankingType;
import app.tennisapp.mapper.RankingMapper;
import app.tennisapp.repository.RankingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RankingServiceTest {
    @Mock
    private RankingRepository rankingRepository;
    @Mock
    private RankingMapper rankingMapper;
    @InjectMocks
    private RankingService rankingService;

    private RankingEntry buildEntry(Long id, int position, RankingType type) {
        return RankingEntry.builder()
                .id(id)
                .player(Player.builder().id(1L).fullName("Novak Djokovic").nationality("Serbia").build())
                .position(position)
                .points(10000)
                .movement("same")
                .rankingType(type)
                .rankingDate(LocalDate.now())
                .build();
    }

    private RankingEntryDto buildEntryDto(Long id, int position, RankingType type) {
        return new RankingEntryDto(id, 1L, "Novak Djokovic", "Serbia",
                position, 10000, "same", type, LocalDate.now());
    }

    // getRankingByType
    @Test
    void shouldReturnAtpRanking() {
        RankingEntry entry = buildEntry(1L, 1, RankingType.ATP);
        RankingEntryDto dto = buildEntryDto(1L, 1, RankingType.ATP);

        when(rankingRepository.findByRankingTypeOrderByPositionAsc(RankingType.ATP))
                .thenReturn(List.of(entry));
        when(rankingMapper.toDto(List.of(entry))).thenReturn(List.of(dto));

        List<RankingEntryDto> result = rankingService.getRankingByType(RankingType.ATP);

        assertNotNull(result);
        assertEquals(List.of(dto), result);
        verify(rankingRepository, times(1)).findByRankingTypeOrderByPositionAsc(RankingType.ATP);
        verify(rankingMapper, times(1)).toDto(List.of(entry));
    }

    @Test
    void shouldReturnWtaRanking() {
        RankingEntry entry = buildEntry(1L, 1, RankingType.WTA);
        RankingEntryDto dto = buildEntryDto(1L, 1, RankingType.WTA);

        when(rankingRepository.findByRankingTypeOrderByPositionAsc(RankingType.WTA))
                .thenReturn(List.of(entry));
        when(rankingMapper.toDto(List.of(entry))).thenReturn(List.of(dto));

        List<RankingEntryDto> result = rankingService.getRankingByType(RankingType.WTA);

        assertNotNull(result);
        assertEquals(List.of(dto), result);
        verify(rankingRepository, times(1)).findByRankingTypeOrderByPositionAsc(RankingType.WTA);
    }

    @Test
    void shouldReturnEmptyList() {
        when(rankingRepository.findByRankingTypeOrderByPositionAsc(RankingType.ATP))
                .thenReturn(List.of());
        when(rankingMapper.toDto(List.of())).thenReturn(List.of());

        List<RankingEntryDto> result = rankingService.getRankingByType(RankingType.ATP);

        assertThat(result).isEmpty();
        verify(rankingRepository, times(1)).findByRankingTypeOrderByPositionAsc(RankingType.ATP);
        verifyNoMoreInteractions(rankingMapper);
    }
}

