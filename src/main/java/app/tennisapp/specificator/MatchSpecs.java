package app.tennisapp.specificator;

import app.tennisapp.entity.Match;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class MatchSpecs {
    public Specification<Match> isLive(Boolean live) {
        return (root, query, cb) ->
                live == null ?
                        cb.conjunction() :
                        cb.equal(root.get("isLive"), live);
    }

    public Specification<Match> hasPlayer(Long playerId) {
        return (root, query, cb) ->
                playerId == null ?
                        cb.conjunction() :
                        cb.or(
                                cb.equal(root.get("firstPlayer").get("id"), playerId),
                                cb.equal(root.get("secondPlayer").get("id"), playerId)
                        );
    }

    public Specification<Match> headToHead(Long firstPlayerId, Long secondPlayerId) {
        return (root, query, cb) ->
                firstPlayerId == null || secondPlayerId == null ?
                        cb.conjunction() :
                        cb.or(
                                cb.and(
                                        cb.equal(root.get("firstPlayer").get("id"), firstPlayerId),
                                        cb.equal(root.get("secondPlayer").get("id"), secondPlayerId)
                                ),
                                cb.and(
                                        cb.equal(root.get("firstPlayer").get("id"), secondPlayerId),
                                        cb.equal(root.get("secondPlayer").get("id"), firstPlayerId)
                                )
                        );
    }

    public Specification<Match> hasTournament(Long tournamentId) {
        return (root, query, cb) ->
                tournamentId == null ?
                        cb.conjunction() :
                        cb.equal(root.get("tournament").get("id"), tournamentId);
    }

    public Specification<Match> dateAfter(LocalDate dateFrom) {
        return (root, query, cb) ->
                dateFrom == null ?
                        cb.conjunction() :
                        cb.greaterThanOrEqualTo(root.get("date"), dateFrom);
    }

    public Specification<Match> dateBefore(LocalDate dateTo) {
        return (root, query, cb) ->
                dateTo == null ?
                        cb.conjunction() :
                        cb.lessThanOrEqualTo(root.get("date"), dateTo);
    }
}