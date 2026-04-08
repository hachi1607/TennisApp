package app.tennisapp.mapper;

import app.tennisapp.dto.RankingEntryDto;
import app.tennisapp.entity.RankingEntry;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RankingMapper {
    public RankingEntryDto toDto(RankingEntry entry) {
        return new RankingEntryDto(
                entry.getId(),
                entry.getPlayer().getId(),
                entry.getPlayer().getFullName(),
                entry.getPlayer().getNationality(),
                entry.getPosition(),
                entry.getPoints(),
                entry.getMovement(),
                entry.getRankingType(),
                entry.getRankingDate()
        );
    }

    public List<RankingEntryDto> toDto(List<RankingEntry> entries) {
        return entries.stream()
                .map(this::toDto)
                .toList();
    }
}