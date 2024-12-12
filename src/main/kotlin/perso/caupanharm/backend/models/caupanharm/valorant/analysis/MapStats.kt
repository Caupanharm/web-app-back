package perso.caupanharm.backend.models.caupanharm.valorant.analysis

data class MapStats(
    val name: String,
    val count: Long,
    val playRate: Double,
    val atkWinrate: Double,
    val defWinrate: Double,
    val topAgents: List<MapStatsAgents>
)

data class MapStatsAgents(
    val name: String,
    val count: Long,
    val playRate: Double,
    val pickRate: Double,
    val winrate: Double,
    val atkWinrate: Double,
    val defWinrate: Double
)
