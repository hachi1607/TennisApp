package app.tennisapp.service;

import app.tennisapp.dto.MatchDto;
import app.tennisapp.entity.Match;
import app.tennisapp.exception.ResourceNotFoundException;
import app.tennisapp.mapper.MatchMapper;
import app.tennisapp.repository.MatchRepository;
import app.tennisapp.specificator.MatchSpecs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {
    private final MatchRepository matchRepository;
    private final MatchMapper matchMapper;
    private final MatchSpecs matchSpecs;

    public Page<MatchDto> getMatches(
            Boolean live,
            Long playerId,
            Long firstPlayerId, Long secondPlayerId,
            Long tournamentId,
            LocalDate dateFrom, LocalDate dateTo,
            Pageable pageable) {

        log.debug("Fetching matches with filters: live={}, playerId={}, tournamentId={}, dateFrom={}, dateTo={}",
                live, playerId, tournamentId, dateFrom, dateTo);

        Specification<Match> spec = Specification.allOf(
                matchSpecs.withRelations(),
                matchSpecs.isLive(live),
                matchSpecs.hasPlayer(playerId),
                matchSpecs.headToHead(firstPlayerId, secondPlayerId),
                matchSpecs.hasTournament(tournamentId),
                matchSpecs.dateAfter(dateFrom),
                matchSpecs.dateBefore(dateTo)
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