package perso.caupanharm.backend.models.caupanharm.valorant.analysis

data class MapStats(
    val name: String,
    val games: Long,
    val pickRate: Double,
    val atkWinrate: Double,
    val defWinrate: Double,
    val topAgents: List<MapStatsAgents>
)

data class MapStatsAgents(
    val name: String,
    val occurences: Long,
    val winrate: Double,
    val atkWinrate: Double,
    val defWinrate: Double
)
