package app.tennisapp.service;

import app.tennisapp.dto.MatchDto;
import app.tennisapp.exception.ResourceNotFoundException;
import app.tennisapp.mapper.MatchMapper;
import app.tennisapp.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchMapper matchMapper;

    public Page<MatchDto> getAllMatches(Pageable pageable) {
        return matchRepository.findAllWithPlayers(pageable)
                .map(matchMapper::toDto);
    }

    public MatchDto getMatchById(Long id) {
        return matchRepository.findById(id)
                .map(matchMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found: " + id));
    }

    public List<MatchDto> getMatchesByDateRange(LocalDate start, LocalDate end) {
        return matchMapper.toDto(matchRepository.findByDateBetweenOrderByDateAscTimeAsc(start, end));
    }

    public List<MatchDto> getLiveMatches() {
        return matchMapper.toDto(matchRepository.findByIsLiveTrue());
    }

    public List<MatchDto> getMatchesByPlayer(Long playerId) {
        return matchMapper.toDto(matchRepository.findByPlayerId(playerId));
    }

    public List<MatchDto> getMatchesByTournament(Long tournamentId) {
        return matchMapper.toDto(matchRepository.findByTournamentIdOrderByDateDesc(tournamentId));
    }

    public List<MatchDto> getMatchesByPlayers(Long firstPlayerId, Long secondPlayerId) {
        return matchMapper.toDto(matchRepository.findByFirstPlayerIdAndSecondPlayerId(firstPlayerId, secondPlayerId));
    }
}