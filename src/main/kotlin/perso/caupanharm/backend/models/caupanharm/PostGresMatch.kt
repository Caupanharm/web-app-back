package perso.caupanharm.backend.models.caupanharm

import jakarta.persistence.*

@Entity
@Table(name = "Matches")
data class PostGresMatch(

    @Id
    @Column(name = "match_id", nullable = false)
    val matchId: String = "",

    @Column(name = "map", nullable = false)
    val map: String = "",

    @Column(name = "game_length_millis", nullable = false)
    val gameLengthMillis: Int = 0,

    @Column(name = "game_start", nullable = false)
    val gameStart: String = "",

    @Column(name = "queue")
    val queue: String? = null,

    @Column(name = "season", nullable = false)
    val season: String = "",

    @Lob
    @Column(name = "players", nullable = false)
    val players: String = "", // JSON serialized List<CaupanharmPlayer>

    @Lob
    @Column(name = "teams", nullable = false)
    val teams: String = "", // JSON serialized List<CaupanharmTeam>

    @Lob
    @Column(name = "rounds", nullable = false)
    val rounds: String = "", // JSON serialized List<CaupanharmRound>

    @Lob
    @Column(name = "kills", nullable = false)
    val kills: String = "" // JSON serialized List<CaupanharmKill>
) {
    // Constructeur par défaut requis par JPA
    constructor() : this(
        matchId = "",
        map = "",
        gameLengthMillis = 0,
        gameStart = "",
        queue = null,
        season = "",
        players = "",
        teams = "",
        rounds = "",
        kills = ""
    )
}
