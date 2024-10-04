package perso.caupanharm.backend.models.valorant.match.full

data class CaupanharmMatchFull(
    val metadata: CaupanharmMatchMetadata,
    val players: List<CaupanharmMatchPlayer>,
    val teams: List<CaupanharmMatchTeam>,
    val rounds: List<CaupanharmMatchRound>,
    val kills: List<CaupanharmMatchKill>
)

data class CaupanharmMatchMetadata(
    val id: String,
    val map: String,
    val gameLengthMillis: Int,
    val gameStart: String,
    val isCompleted: Boolean,
    val queue: String?,
    val season: String
)

data class CaupanharmMatchPlayer(
    val id: String,
    val name: String, // includes tag
    val team: String,
    val party: String,
    val rank: String?,
    val agent: String,
    val stats: CaupanharmPlayerStats,
    val abilityCasts: CaupanharmAbilities,
    val behavior: BehaviorSummary,
    val totalEconomy: TotalEconomy
)

data class CaupanharmPlayerStats(
    val score: Int,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val headshots: Int,
    val bodyshots: Int,
    val legshots: Int,
    val damageDealt: Int,
    val damageReceived: Int
)

data class CaupanharmAbilities(
    val ability1: Int,
    val ability2: Int,
    val ability3: Int, // noted as "grenade" on Riot's and Henrik's side
    val ultimate: Int
)

data class BehaviorSummary(
    val afk: Int,
    val dealtFriendlyFire: Int,
    val receivedFriendlyFire: Int,
    val inSpawn: Int
)

data class RoundBehavior(
    val afk: Boolean,
    val penalised: Boolean,
    val inSpawn: Boolean
)

data class TotalEconomy(
    val spent: Int,
    val loadout: Int
)

data class RoundEconomy(
    val loadoutValue: Int,
    val remaining: Int,
    val weapon: String?,
    val armor: String?
)

data class CaupanharmMatchTeam(
    val id: String,
    val allyScore: Int,
    val enemyScore: Int
)

data class CaupanharmMatchRound(
    val winningTeam: String,
    val result: String, // issue du round: elimination, defuse, detonate, ""
    val ceremony: String, // clutch, thrifty, etc
    val plantEvent: BombEvent?,
    val defuseEvent: BombEvent?,
    val stats: List<RoundPlayerStats>
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
    val viewAngle: Double // radians
)

data class Location(
    val x: Int,
    val y: Int
)

data class RoundPlayerStats(
    val player: String,
    val abilityCasts: CaupanharmAbilities,
    val damageEvents: List<DamageEvent>,
    val stats: RoundPlayerStatsRecap,
    val economy: RoundEconomy,
    val behavior: RoundBehavior,
)

data class RoundPlayerStatsRecap(
    val score: Int,
    val kills: Int,
    val headshots: Int,
    val bodyshots: Int,
    val legshots: Int
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
    val killer: String,
    val victim: String,
    val assistants: List<String>?,
    val location: Location?,
    val weapon: String?,
    val secondaryFire: Boolean,
    val playerLocations: List<PlayerLocation>
)
