package perso.caupanharm.backend.models.caupanharm.valorant.database

import jakarta.persistence.*

@Entity
@Table(name = "agents_stats")
data class PostGresAgentsStats(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int = 0,

    val map: String?,

    val agent: String?,

    @Column(name = "games_played")
    val gamesPlayed: Int,

    @Column(name = "play_rate")
    val playRate: Double,

    @Column(name = "pick_rate")
    val pickRate: Double?,

    @Column(name = "win_rate")
    val winRate: Double?,

    @Column(name = "attack_win_rate")
    val atkWinRate: Double,

    @Column(name = "defense_win_rate")
    val defWinRate: Double
){
    constructor() : this(
        map = null,
        agent = null,
        gamesPlayed = 0,
        playRate = 0.0,
        pickRate = null,
        winRate = null,
        atkWinRate = 0.0,
        defWinRate = 0.0
    )
}