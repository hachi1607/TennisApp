package app.tennisapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "ranking_entries")
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class RankingEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    private Long externalId;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private int points;

    private String movement;

    @Enumerated(EnumType.STRING) // wrzucanie enumów do db jako string value
    @Column(nullable = false)
    private RankingType rankingType;

    @Column(nullable = false)
    private LocalDate rankingDate;
}