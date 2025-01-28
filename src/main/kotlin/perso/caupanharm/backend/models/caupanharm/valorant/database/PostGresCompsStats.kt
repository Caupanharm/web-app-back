package perso.caupanharm.backend.models.caupanharm.valorant.database

import jakarta.persistence.*

@Entity
@Table(name = "comps_stats")
class PostGresCompsStats(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int = 0,

    val map: String?,

    val composition: List<String>,

    @Column(name = "bayesian_average")
    val bayesianAverage: Double,

    @Column(name = "games_played")
    val gamesPlayed: Int,

    @Column(name = "play_rate")
    val playRate: Double,

    @Column(name = "pick_rate")
    val pickRate: Double,

    @Column(name = "win_rate")
    val winRate: Double
) {
    constructor() : this(
        map = null,
        composition = emptyList(),
        bayesianAverage = 0.0,
        gamesPlayed = 0,
        playRate = 0.0,
        pickRate = 0.0,
        winRate = 0.0
    )
}

class PostGresCompsStatsComputed(
    val composition: List<String>,
    val gamesPlayed: Int,
    val playRate: Double,
    val pickRate: Double,
    val winRate: Double
)