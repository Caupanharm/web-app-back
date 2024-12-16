package perso.caupanharm.backend.models.caupanharm.valorant.database

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "matches_xs")
data class PostGresMatchXS(
    @Id
    @Column(name = "match_id")
    val matchId: String,

    val date: Instant,

    val map: String,

    val rank: Int,

    @Column(name = "blue_score")
    val blueScore: Int,

    @Column(name = "red_score")
    val redScore: Int,

    @Column(name = "blue_score_atk")
    val blueScoreAttack: Int,

    @Column(name = "blue_score_def")
    val blueScoreDefense: Int,

    @Column(name = "red_score_atk")
    val redScoreAttack: Int,

    @Column(name = "red_score_def")
    val redScoreDefense: Int,
){
    constructor(): this(
        matchId = "",
        date = Instant.now(),
        map = "",
        rank = 0,
        blueScore = 0,
        redScore = 0,
        blueScoreAttack = 0,
        blueScoreDefense = 0,
        redScoreAttack = 0,
        redScoreDefense = 0
    )
}

@Entity
@Table(name = "matches_xs_agents")
data class PostGresMatchXSPlayer(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int = 0,

    @Column(name = "player_id")
    val playerId: String,

    val rank: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    val matchXS: PostGresMatchXS,

    val agent: String,

    @Column(name = "agent_class")
    val agentClass: String,

    @Column(name = "team", length = 4)
    val team: String
){
    constructor(): this(
        matchXS = PostGresMatchXS(
            matchId = "",
            date = Instant.now(),
            map = "",
            rank = 0,
            blueScore = 0,
            redScore = 0,
            blueScoreAttack = 0,
            blueScoreDefense = 0,
            redScoreAttack = 0,
            redScoreDefense = 0
        ),
        playerId = "",
        rank = 0,
        agent = "",
        agentClass = "",
        team = ""
    )
}
