package app.tennisapp.service;

import app.tennisapp.client.ApiTennisClient;
import app.tennisapp.client.response.ApiMatchDto;
import app.tennisapp.client.response.ApiPlayerDto;
import app.tennisapp.client.response.ApiPlayerStatsDto;
import app.tennisapp.client.response.ApiStandingDto;
import app.tennisapp.client.response.ApiTournamentDto;
import app.tennisapp.entity.*;
import app.tennisapp.mapper.ApiMatchMapper;
import app.tennisapp.mapper.ApiPlayerMapper;
import app.tennisapp.mapper.ApiRankingMapper;
import app.tennisapp.mapper.ApiTournamentMapper;
import app.tennisapp.repository.MatchRepository;
import app.tennisapp.repository.PlayerRepository;
import app.tennisapp.repository.PlayerSeasonStatsRepository;
import app.tennisapp.repository.RankingRepository;
import app.tennisapp.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static app.tennisapp.mapper.ApiParseUtils.parseLong;
import static app.tennisapp.mapper.ApiParseUtils.parseMatchDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {
    private final ApiTennisClient apiTennisClient;

    private final TournamentRepository tournamentRepository;
    private final PlayerRepository playerRepository;
    private final PlayerSeasonStatsRepository playerSeasonStatsRepository;
    private final MatchRepository matchRepository;
    private final RankingRepository rankingRepository;

    private final ApiTournamentMapper apiTournamentMapper;
    private final ApiPlayerMapper apiPlayermapper;
    private final ApiRankingMapper apiRankingMapper;
    private final ApiMatchMapper apiMatchMapper;

    @Transactional
    public void syncTournaments() {
        List<ApiTournamentDto> tournaments = apiTennisClient.fetchTournaments();
        int processed = 0;

        for (ApiTournamentDto dto : tournaments) {
            EventCategory category = EventCategory.fromApiString(dto.eventTypeType());
            if (category == null) {
                continue;
            }

            Long externalId = parseLong(dto.tournamentKey());
            if (externalId == null) {
                continue;
            }

            Tournament tournament = tournamentRepository.findByExternalId(externalId)
                    .map(existing -> apiTournamentMapper.updateEntity(existing, dto, category))
                    .orElseGet(() -> apiTournamentMapper.toEntity(dto, externalId, category));

            tournamentRepository.save(tournament);
            processed++;
        }

        log.info("Tournaments sync finished: processed={}", processed);
    }

    @Transactional
    public void syncPlayer(Long externalPlayerKey) {
        ApiPlayerDto dto = apiTennisClient.fetchPlayer(externalPlayerKey);

        Player player = playerRepository.findByExternalId(externalPlayerKey)
                .map(existing -> apiPlayermapper.updateEntity(existing, dto))
                .orElseGet(() -> apiPlayermapper.toEntity(dto, externalPlayerKey));

        player = playerRepository.save(player);

        if (dto.stats() != null) {
            for (ApiPlayerStatsDto statDto : dto.stats()) {
                if (statDto.season() == null || statDto.season().isBlank()
                        || statDto.type() == null || statDto.type().isBlank()) {
                    continue;
                }

                Player finalPlayer = player;
                PlayerSeasonStats stats = playerSeasonStatsRepository
                        .findByPlayerIdAndSeasonAndType(player.getId(), statDto.season(), statDto.type())
                        .map(existing -> apiPlayermapper.updateStatsEntity(existing, statDto))
                        .orElseGet(() -> apiPlayermapper.toStatsEntity(statDto, finalPlayer));

                playerSeasonStatsRepository.save(stats);
            }
        }

        log.info("Player sync finished: externalId={}", externalPlayerKey);
    }

    @Transactional
    public void syncFixtures(LocalDate dateStart, LocalDate dateStop) {
        syncMatchesInternal(
                apiTennisClient.fetchFixtures(dateStart, dateStop),
                "fixtures " + dateStart + " to " + dateStop
        );
    }

    @Transactional
    public void syncLivescores() {
        syncMatchesInternal(apiTennisClient.fetchLivescores(), "livescores");
    }

    @Transactional
    public void syncStandings() {
        LocalDate today = LocalDate.now();
        int processed = 0;

        for (RankingType type : RankingType.values()) {
            List<ApiStandingDto> standings = apiTennisClient.fetchStandings(type.name());
            rankingRepository.deleteByRankingType(type);

            for (ApiStandingDto dto : standings) {
                Long playerExternalId = parseLong(dto.playerKey());
                if (playerExternalId == null) {
                    continue;
                }

                Player player = playerRepository.findByExternalId(playerExternalId)
                        .orElseGet(() -> playerRepository.save(
                                apiRankingMapper.toPlayerSkeleton(dto, playerExternalId)));

                rankingRepository.save(apiRankingMapper.toEntity(dto, player, type, today));
                processed++;
            }
        }

        log.info("Standings sync finished: processed={}", processed);
    }

    private void syncMatchesInternal(List<ApiMatchDto> matches, String context) {
        int processed = 0;
        int skipped = 0;

        for (ApiMatchDto dto : matches) {
            EventCategory category = EventCategory.fromApiString(dto.eventTypeType());
            if (category == null) {
                skipped++;
                continue;
            }

            Long matchExternalId = parseLong(dto.eventKey());
            Long tournamentExternalId = parseLong(dto.tournamentKey());
            Long firstPlayerKey = parseLong(dto.firstPlayerKey());
            Long secondPlayerKey = parseLong(dto.secondPlayerKey());

            if (matchExternalId == null || tournamentExternalId == null
                    || firstPlayerKey == null || secondPlayerKey == null) {
                skipped++;
                continue;
            }

            Tournament tournament = tournamentRepository.findByExternalId(tournamentExternalId)
                    .orElseGet(() -> tournamentRepository.save(Tournament.builder()
                            .externalId(tournamentExternalId)
                            .name(dto.tournamentName())
                            .eventCategory(category)
                            .build()));

            Player firstPlayer = findOrCreatePlayer(firstPlayerKey, dto.eventFirstPlayer());
            Player secondPlayer = findOrCreatePlayer(secondPlayerKey, dto.eventSecondPlayer());

            if (parseMatchDate(dto.eventDate()) == null) {
                skipped++;
                continue;
            }

            Match match = matchRepository.findByExternalId(matchExternalId)
                    .map(existing -> apiMatchMapper.updateEntity(existing, dto, firstPlayer, secondPlayer, tournament))
                    .orElseGet(() -> apiMatchMapper.toEntity(dto, matchExternalId, firstPlayer, secondPlayer, tournament));

            matchRepository.save(match);
            processed++;
        }

        log.info("{} sync finished: processed={}, skipped={}", context, processed, skipped);
    }

    private Player findOrCreatePlayer(Long externalId, String fullName) {
        return playerRepository.findByExternalId(externalId)
                .orElseGet(() -> playerRepository.save(Player.builder()
                        .externalId(externalId)
                        .fullName(fullName)
                        .build()));
    }
}