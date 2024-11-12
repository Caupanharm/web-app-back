package perso.caupanharm.backend.models.caupanharm

import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import perso.caupanharm.backend.models.caupanharm.valorant.match.full.*

@Entity
@Table(name = "matches")
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

    @Type(JsonType::class)
    @Column(name = "players", columnDefinition = "jsonb", nullable = false)
    val players: List<CaupanharmMatchPlayer> = emptyList(),

    @Type(JsonType::class)
    @Column(name = "teams", columnDefinition = "jsonb", nullable = false)
    val teams: List<CaupanharmMatchTeam> = emptyList(),

    @Type(JsonType::class)
    @Column(name = "rounds", columnDefinition = "jsonb", nullable = false)
    val rounds: List<CaupanharmMatchRound> = emptyList(),

    @Type(JsonType::class)
    @Column(name = "kills", columnDefinition = "jsonb", nullable = false)
    val kills: List<CaupanharmMatchKill> = emptyList()
) {

    constructor() : this(
        matchId = "",
        map = "",
        gameLengthMillis = 0,
        gameStart = "",
        queue = null,
        season = "",
        players = emptyList(),
        teams = emptyList(),
        rounds = emptyList(),
        kills = emptyList()
    )

    fun toCaupanharmMatchFull(): CaupanharmMatchFull {
        return CaupanharmMatchFull(
            CaupanharmMatchMetadata(
                matchId,
                map,
                gameLengthMillis,
                gameStart,
                true,
                queue,
                season
            ),
            players,
            teams,
            rounds,
            kills
        )
    }
}
