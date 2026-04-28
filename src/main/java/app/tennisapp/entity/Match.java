package app.tennisapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NamedEntityGraph(
        name = "Match.withRelations",
        attributeNodes = {
                @NamedAttributeNode("firstPlayer"),
                @NamedAttributeNode("secondPlayer"),
                @NamedAttributeNode("tournament")
        }
)
@Table(name = "matches")
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    private Long externalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "first_player_id", nullable = false)
    private Player firstPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "second_player_id", nullable = false)
    private Player secondPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    private String season;

    @Column(nullable = false)
    private LocalDate date;

    private String time;

    private String finalResult;

    private String gameResult;

    private String eventServe;

    private String winner;

    private String status;

    private String round;

    @Column(nullable = false)
    private boolean isLive;

    @Column(nullable = false)
    private boolean qualification;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String scoresJson;

    private LocalDateTime lastSyncedAt;
}