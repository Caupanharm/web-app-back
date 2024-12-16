package perso.caupanharm.backend.models.caupanharm.valorant.match

import perso.caupanharm.backend.models.caupanharm.valorant.database.PostGresMatch
import perso.caupanharm.backend.models.caupanharm.valorant.database.PostGresMatchXS
import perso.caupanharm.backend.models.caupanharm.valorant.database.PostGresMatchXSPlayer
import perso.caupanharm.backend.models.riot.assets.Agents
import java.time.Instant
import kotlin.math.roundToInt

data class CaupanharmMatchFull(
    val metadata: CaupanharmMatchMetadata,
    val players: List<CaupanharmMatchPlayer>,
    val score: CaupanharmMatchScore,
    val rounds: List<CaupanharmMatchRound>,
    val kills: List<CaupanharmMatchKill>
){
    fun toPostgresMatch(): PostGresMatch {
        return PostGresMatch(
            metadata.matchId,
            metadata.map,
            metadata.gameLengthMillis,
            metadata.gameStartMillis,
            metadata.queue,
            metadata.season,
            players,
            score,
            rounds,
            kills
        )
    }

    fun toPostgresMatchXS(): PostGresMatchXS{
        return PostGresMatchXS(
            matchId  = metadata.matchId,
            date = Instant.ofEpochMilli(metadata.gameStartMillis),
            map = metadata.map,
            rank = (players.filter { it.rank != 0 }.sumOf { it.rank }.toDouble() / players.count { it.rank != 0 }).roundToInt(),
            blueScore = score.blue,
            redScore = score.red,
            blueScoreAttack = score.blueAttack,
            blueScoreDefense = score.blueDefense,
            redScoreAttack = score.redAttack,
            redScoreDefense = score.redDefense
        )
    }

    fun toPostgresMatchXSAgents(): List<PostGresMatchXSPlayer>{
        return players.map{ player ->
            PostGresMatchXSPlayer(
                matchXS = toPostgresMatchXS(),
                playerId = player.playerId,
                rank = player.rank,
                agent = player.agent,
                agentClass = Agents.getCategoryFromDisplayName(player.agent),
                team = player.team
            )
        }
    }
}

data class CaupanharmMatchMetadata(
    val matchId: String,
    val map: String,
    val gameLengthMillis: Int,
    val gameStartMillis: Long,
    val queue: String?,
    val season: String
)

data class CaupanharmMatchPlayer(
    val playerId: String,
    val name: String, // includes tag
    val team: String,
    val party: String,
    val rank: Int,
    val agent: String,
    val stats: CaupanharmPlayerStats,
    val abilityCasts: CaupanharmAbilities,
    val behavior: BehaviorSummary,
)

data class CaupanharmPlayerStats(
    val score: Int,
    val kills: Int,
    val deaths: Int,
    val assists: Int
)

data class CaupanharmAbilities(
    val ability1: Int?,
    val ability2: Int?,
    val ability3: Int?, // noted as "grenade" on Riot's and Henrik's side
    val ultimate: Int?
)

data class BehaviorSummary(
    val afk: Int,
    val dealtFriendlyFire: Int,
    val inSpawn: Int
)

data class RoundBehavior(
    val afk: Boolean,
    val penalised: Boolean,
    val inSpawn: Boolean
)

data class RoundEconomy(
    val loadoutValue: Int,
    val spent: Int,
    val remaining: Int,
    val weapon: String?,
    val armor: String?
)

data class CaupanharmMatchScore(
    val blue: Int,
    val red: Int,
    val blueAttack: Int,
    val blueDefense: Int,
    val redAttack: Int,
    val redDefense: Int
)

data class CaupanharmMatchRound(
    val winningTeam: String,
    val result: String, // issue du round: elimination, defuse, detonate, ""
    val ceremony: String, // clutch, thrifty, etc
    val plantEvent: BombEvent?,
    val defuseEvent: BombEvent?,
    val stats: List<CaupanharmRoundPlayerStats>
)

data class BombEvent(
    val roundTimeMillis: Int,
    val site: String?,
    val location: Location?,
    val player: String,
    val playersLocation: List<PlayerLocation>?,
)

data class PlayerLocation(
    val player: String,
    val location: Location?,
    val viewRadians: Double // radians
)

data class Location(
    val x: Int,
    val y: Int
)

data class CaupanharmRoundPlayerStats(
    val player: String,
    val abilityCasts: CaupanharmAbilities,
    val damageEvents: List<DamageEvent>,
    val score: Int,
    val kills: List<CaupanharmMatchKill>,
    val economy: RoundEconomy,
    val behavior: RoundBehavior,
)

data class DamageEvent(
    val player: String,
    val damage: Int,
    val headshots: Int,
    val bodyshots: Int,
    val legshots: Int
)

data class CaupanharmMatchKill(
    val round: Int,
    val roundTimeMillis: Int,
    val matchTimeMillis: Int,
    val killer: String?,
    val victim: String,
    val assistants: List<String>?,
    val location: Location?,
    val weapon: CaupanharmFinishingDamage,
    val playerLocations: List<PlayerLocation>
)

data class CaupanharmFinishingDamage(
    val damageType: String?,
    val damageItem: String?,
    val isSecondaryFireMode: Boolean
)
