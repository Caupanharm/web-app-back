package perso.caupanharm.backend.models.caupanharm.valorant.analysis

data class MapStats(
    val name: String?,
    val gamesPlayed: Int,
    val playRate: Double?,
    val atkWinrate: Double,
    val defWinrate: Double,
    var topAgents: MutableList<MapStatsAgents>
)

data class MapStatsAgents(
    val name: String,
    val gamesPlayed: Int,
    val playRate: Double,
    val pickRate: Double,
    val winrate: Double,
    val atkWinrate: Double,
    val defWinrate: Double
)

