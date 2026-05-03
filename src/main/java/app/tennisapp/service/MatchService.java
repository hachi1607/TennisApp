package app.tennisapp.service;

import app.tennisapp.dto.MatchDto;
import app.tennisapp.entity.Match;
import app.tennisapp.exception.ResourceNotFoundException;
import app.tennisapp.mapper.MatchMapper;
import app.tennisapp.params.MatchFilterParams;
import app.tennisapp.repository.MatchRepository;
import app.tennisapp.specificator.MatchSpecs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {
    private final MatchRepository matchRepository;
    private final MatchMapper matchMapper;
    private final MatchSpecs matchSpecs;

    public Page<MatchDto> getMatches(MatchFilterParams params, Pageable pageable) {
        log.debug("Fetching matches with filters: live={}, playerId={}, tournamentId={}, dateFrom={}, dateTo={}",
                params.live(), params.playerId(), params.tournamentId(), params.dateFrom(), params.dateTo());

        Specification<Match> spec = Specification.allOf(
                matchSpecs.isLive(params.live()),
                matchSpecs.hasPlayer(params.playerId()),
                matchSpecs.headToHead(params.firstPlayerId(), params.secondPlayerId()),
                matchSpecs.hasTournament(params.tournamentId()),
                matchSpecs.dateAfter(params.dateFrom()),
                matchSpecs.dateBefore(params.dateTo())
        );

        return matchRepository.findAll(spec, pageable)
                .map(matchMapper::toDto);
    }

    public MatchDto getMatchById(Long id) {
        log.debug("Fetching match id={}", id);
        return matchRepository.getMatchByIdWithPlayers(id)
                .map(matchMapper::toDto)
                .orElseThrow(() -> {
                    log.warn("Match not found: id={}", id);
                    return new ResourceNotFoundException("Match not found: " + id);
                });
    }
}