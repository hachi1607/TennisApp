package app.tennisapp.service;

import app.tennisapp.dto.RankingEntryDto;
import app.tennisapp.entity.RankingType;
import app.tennisapp.mapper.RankingMapper;
import app.tennisapp.repository.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingService {
    private final RankingRepository rankingRepository;
    private final RankingMapper rankingMapper;

    @Transactional(readOnly = true)
    public List<RankingEntryDto> getRankingByType(RankingType type) {
        return rankingMapper.toDto(rankingRepository.findByRankingTypeOrderByPositionAsc(type));
    }
}