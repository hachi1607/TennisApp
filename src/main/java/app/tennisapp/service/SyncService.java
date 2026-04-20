package app.tennisapp.service;

import app.tennisapp.client.ApiTennisClient;
import app.tennisapp.client.response.ApiMatchDto;
import app.tennisapp.client.response.ApiPlayerDto;
import app.tennisapp.client.response.ApiPlayerStatsDto;
import app.tennisapp.client.response.ApiStandingDto;
import app.tennisapp.client.response.ApiTournamentDto;
import app.tennisapp.entity.*;
import app.tennisapp.exception.ResourceNotFoundException;
import app.tennisapp.repository.MatchRepository;
import app.tennisapp.repository.PlayerRepository;
import app.tennisapp.repository.PlayerSeasonStatsRepository;
import app.tennisapp.repository.RankingRepository;
import app.tennisapp.repository.TournamentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {
    private final ApiTennisClient apiTennisClient;
    private final ObjectMapper objectMapper;

    private final TournamentRepository tournamentRepository;
    private final PlayerRepository playerRepository;
    private final PlayerSeasonStatsRepository playerSeasonStatsRepository;
    private final MatchRepository matchRepository;
    private final RankingRepository rankingRepository;
    private final SyncLogService syncLogService;

    @Transactional
    public void syncTournaments() {
        List<ApiTournamentDto> tournaments = apiTennisClient.fetchTournaments();
        if (tournaments.isEmpty()) {
            throw new IllegalStateException("No tournaments returned from API Tennis");
        }

        int processedEntities = 0;

        for (ApiTournamentDto dto : tournaments) {
            EventCategory category = EventCategory.fromApiString(dto.eventTypeType());
            if (category == null) {
                log.debug("Skipping tournament with unsupported category: " + dto.eventTypeType());
                continue;
            }

            Long externalId = parseLong(dto.tournamentKey());
            if (externalId == null) {
                log.debug("Skipping tournament with missing tournament_key");
                continue;
            }

            Tournament tournament = tournamentRepository.findByExternalId(externalId)
                    .map(existing -> existing.toBuilder()
                            .name(dto.tournamentName())
                            .eventCategory(category)
                            .build())
                    .orElseGet(() -> Tournament.builder()
                            .externalId(externalId)
                            .name(dto.tournamentName())
                            .eventCategory(category)
                            .build());

            tournamentRepository.save(tournament);
            processedEntities++;
        }

        log.info("Tournaments sync finished: processedEntities=" + processedEntities);
        syncLogService.logSync(SyncEntityType.TOURNAMENT, SyncStatus.SUCCESS, null);
    }

    @Transactional
    public void syncPlayer(Long externalPlayerKey) {
        try {
            ApiPlayerDto dto = apiTennisClient.fetchPlayer(externalPlayerKey)
                    .orElseThrow(() -> new ResourceNotFoundException("No player data for key " + externalPlayerKey));

            Player player = playerRepository.findByExternalId(externalPlayerKey)
                    .map(existing -> existing.toBuilder()
                            .fullName(dto.playerName())
                            .nationality(dto.playerCountry())
                            .imageUrl(dto.playerLogo())
                            .birthDate(parseBirthDate(dto.playerBday()))
                            .lastSyncedAt(LocalDateTime.now())
                            .build())
                    .orElseGet(() -> Player.builder()
                            .externalId(externalPlayerKey)
                            .fullName(dto.playerName())
                            .nationality(dto.playerCountry())
                            .imageUrl(dto.playerLogo())
                            .birthDate(parseBirthDate(dto.playerBday()))
                            .lastSyncedAt(LocalDateTime.now())
                            .build());

            player = playerRepository.save(player);

            if (dto.stats() != null) {
                for (ApiPlayerStatsDto statDto : dto.stats()) {
                    if (statDto.season() == null || statDto.season().isBlank()
                            || statDto.type() == null || statDto.type().isBlank()) continue;

                    Player finalPlayer = player;
                    PlayerSeasonStats stats = playerSeasonStatsRepository
                            .findByPlayerIdAndSeasonAndType(player.getId(), statDto.season(), statDto.type())
                            .map(existing -> existing.toBuilder()
                                    .rank(parseInt(statDto.rank()))
                                    .titles(parseInt(statDto.titles()))
                                    .matchesWon(parseInt(statDto.matchesWon()))
                                    .matchesLost(parseInt(statDto.matchesLost()))
                                    .hardWon(parseInt(statDto.hardWon()))
                                    .hardLost(parseInt(statDto.hardLost()))
                                    .clayWon(parseInt(statDto.clayWon()))
                                    .clayLost(parseInt(statDto.clayLost()))
                                    .grassWon(parseInt(statDto.grassWon()))
                                    .grassLost(parseInt(statDto.grassLost()))
                                    .build())
                            .orElseGet(() -> PlayerSeasonStats.builder()
                                    .player(finalPlayer)
                                    .season(statDto.season())
                                    .type(statDto.type())
                                    .rank(parseInt(statDto.rank()))
                                    .titles(parseInt(statDto.titles()))
                                    .matchesWon(parseInt(statDto.matchesWon()))
                                    .matchesLost(parseInt(statDto.matchesLost()))
                                    .hardWon(parseInt(statDto.hardWon()))
                                    .hardLost(parseInt(statDto.hardLost()))
                                    .clayWon(parseInt(statDto.clayWon()))
                                    .clayLost(parseInt(statDto.clayLost()))
                                    .grassWon(parseInt(statDto.grassWon()))
                                    .grassLost(parseInt(statDto.grassLost()))
                                    .build());

                    playerSeasonStatsRepository.save(stats);
                }
            }

            log.info("Player sync finished: externalId=" + externalPlayerKey);
            syncLogService.logSync(SyncEntityType.PLAYER, SyncStatus.SUCCESS, null);
        } catch (Exception e) {
            log.error("Failed to sync player externalId=" + externalPlayerKey, e);
            syncLogService.logSync(SyncEntityType.PLAYER, SyncStatus.FAILED, e.getMessage());
        }
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
        try {
            LocalDate today = LocalDate.now();
            int processed = 0;

            for (RankingType type : RankingType.values()) {
                List<ApiStandingDto> standings = apiTennisClient.fetchStandings(type.name());
                if (standings.isEmpty()) {
                    throw new IllegalStateException("No " + type + " standings returned from API Tennis");
                }

                rankingRepository.deleteByRankingType(type);

                for (ApiStandingDto dto : standings) {
                    Long playerExternalId = parseLong(dto.playerKey());
                    if (playerExternalId == null) {
                        continue;
                    }

                    Player player = playerRepository.findByExternalId(playerExternalId)
                            .orElseGet(() -> playerRepository.save(Player.builder()
                                    .externalId(playerExternalId)
                                    .fullName(dto.player())
                                    .nationality(dto.country())
                                    .build()));

                    RankingEntry entry = RankingEntry.builder()
                            .player(player)
                            .position(parseInt(dto.place()))
                            .points(parseInt(dto.points()))
                            .movement(dto.movement())
                            .rankingType(type)
                            .rankingDate(today)
                            .build();

                    rankingRepository.save(entry);
                    processed++;
                }
            }

            log.info("Standings sync finished: processed={}", processed);
            syncLogService.logSync(SyncEntityType.RANKING, SyncStatus.SUCCESS, null);
        } catch (Exception e) {
            log.error("Failed to sync standings", e);
            syncLogService.logSync(SyncEntityType.RANKING, SyncStatus.FAILED, e.getMessage());
        }
    }

    private void syncMatchesInternal(List<ApiMatchDto> matches, String context) {
        try {
            if (matches.isEmpty()) {
                throw new IllegalStateException("No matches returned from API Tennis for " + context);
            }

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

                LocalDate matchDate = parseMatchDate(dto.eventDate());
                if (matchDate == null) {
                    skipped++;
                    continue;
                }

                boolean isLive = !"0".equals(dto.eventLive());
                boolean qualification = "True".equalsIgnoreCase(dto.eventQualification());
                String scoresJson = serializeScores(dto);

                Match match = matchRepository.findByExternalId(matchExternalId)
                        .map(existing -> existing.toBuilder()
                                .firstPlayer(firstPlayer)
                                .secondPlayer(secondPlayer)
                                .tournament(tournament)
                                .season(dto.tournamentSeason())
                                .date(matchDate)
                                .time(dto.eventTime())
                                .finalResult(dto.eventFinalResult())
                                .gameResult(dto.eventGameResult())
                                .eventServe(dto.eventServe())
                                .winner(dto.eventWinner())
                                .status(dto.eventStatus())
                                .round(dto.tournamentRound())
                                .isLive(isLive)
                                .qualification(qualification)
                                .scoresJson(scoresJson)
                                .lastSyncedAt(LocalDateTime.now())
                                .build())
                        .orElseGet(() -> Match.builder()
                                .externalId(matchExternalId)
                                .firstPlayer(firstPlayer)
                                .secondPlayer(secondPlayer)
                                .tournament(tournament)
                                .season(dto.tournamentSeason())
                                .date(matchDate)
                                .time(dto.eventTime())
                                .finalResult(dto.eventFinalResult())
                                .gameResult(dto.eventGameResult())
                                .eventServe(dto.eventServe())
                                .winner(dto.eventWinner())
                                .status(dto.eventStatus())
                                .round(dto.tournamentRound())
                                .isLive(isLive)
                                .qualification(qualification)
                                .scoresJson(scoresJson)
                                .lastSyncedAt(LocalDateTime.now())
                                .build());

                matchRepository.save(match);
                processed++;
            }

            log.info(context + " sync finished: processed=" + processed + ", skipped=" + skipped);
            syncLogService.logSync(SyncEntityType.MATCH, SyncStatus.SUCCESS, null);
        } catch (Exception e) {
            log.error("Failed to sync {}", context, e);
            syncLogService.logSync(SyncEntityType.MATCH, SyncStatus.FAILED, e.getMessage());
        }
    }

    private String serializeScores(ApiMatchDto dto) {
        if (dto.scores() == null || dto.scores().isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(dto.scores());
        } catch (Exception e) {
            log.warn("Could not serialize scores for match " + dto.eventKey());
            return null;
        }
    }

    private Player findOrCreatePlayer(Long externalId, String fullName) {
        return playerRepository.findByExternalId(externalId)
                .orElseGet(() -> playerRepository.save(Player.builder()
                        .externalId(externalId)
                        .fullName(fullName)
                        .build()));
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            long parsed = Long.parseLong(value);
            return parsed == 0L ? null : parsed;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parseInt(String value) {
        if (value == null || value.isBlank()) return 0;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private LocalDate parseMatchDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw);
        } catch (Exception e) {
            log.warn("Could not parse match date: " + raw);
            return null;
        }
    }

    private LocalDate parseBirthDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (Exception e) {
            log.warn("Could not parse birth date: " + raw);
            return null;
        }
    }
}