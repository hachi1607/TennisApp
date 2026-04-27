package app.tennisapp.service;

import app.tennisapp.dto.TournamentDto;
import app.tennisapp.entity.EventCategory;
import app.tennisapp.exception.ResourceNotFoundException;
import app.tennisapp.mapper.TournamentMapper;
import app.tennisapp.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final TournamentMapper tournamentMapper;

    public List<TournamentDto> getTournaments(EventCategory category, String name) {
        if (category != null && name != null) {
            return tournamentMapper.toDto(
                    tournamentRepository.findByEventCategoryAndNameContainingIgnoreCase(category, name));
        }
        if (category != null) {
            return tournamentMapper.toDto(
                    tournamentRepository.findByEventCategory(category));
        }
        if (name != null) {
            return tournamentMapper.toDto(
                    tournamentRepository.findByNameContainingIgnoreCase(name));
        }
        return tournamentMapper.toDto(tournamentRepository.findAll());
    }

    public TournamentDto getTournamentById(Long id) {
        return tournamentRepository.findById(id)
                .map(tournamentMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found: " + id));
    }
}