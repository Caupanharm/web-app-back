package perso.caupanharm.backend.models.caupanharm.valorant.match.full

import perso.caupanharm.backend.models.caupanharm.valorant.database.PostGresMatch

data class CaupanharmMatchFull(
    val metadata: CaupanharmMatchMetadata,
    val players: List<CaupanharmMatchPlayer>,
    val score: CaupanharmMatchScore,
    val rounds: List<CaupanharmMatchRound>,
    val kills: List<CaupanharmMatchKill>
){
    fun toPostgresMatch(): PostGresMatch {
        return PostGresMatch(
            metadata.id,
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
}

data class CaupanharmMatchMetadata(
    val id: String,
    val map: String,
    val gameLengthMillis: Int,
    val gameStartMillis: Long,
    val queue: String?,
    val season: String
)

data class CaupanharmMatchPlayer(
    val id: String,
    val name: String, // includes tag
    val team: String,
    val party: String,
    val rank: Int?,
    val agent: String,
    val stats: CaupanharmPlayerStats,
    val abilityCasts: CaupanharmAbilities,
    val behavior: BehaviorSummary,
)

data class CaupanharmPlayerStats(
    val score: Int,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val playtimeMillis: Int
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
    val red: Int
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
    val roundTimeMillis: Long,
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
    val kills: Int,
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
    val roundTimeMillis: Long,
    val matchTimeMillis: Long,
    val killer: String,
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
