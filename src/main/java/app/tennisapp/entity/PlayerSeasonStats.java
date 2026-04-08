package app.tennisapp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "player_season_stats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"player_id", "season", "type"}))
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSeasonStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false)
    private String season;

    @Column(nullable = false)
    private String type;

    private int rank;

    private int titles;

    private int matchesWon;

    private int matchesLost;

    private int hardWon;

    private int hardLost;

    private int clayWon;

    private int clayLost;

    private int grassWon;

    private int grassLost;
}