package perso.caupanharm.backend.models.caupanharm.valorant.analysis

data class CompStatsResponse(
    val settings: CompStatsSettings,
    val data: List<CompStats>
)

data class CompStatsSettings(
    val map: String?,
    val agents: List<String>,
    val sortType: String,
    val confidence: Long,
    val minCount: Long
)

data class CompStats(
    val comp: List<String>,
    val bayesianAverage: Double,
    val count: Long,
    val winRate: Double
)

data class CompStatsCount(
    var count: Long,
    var wins: Long
)