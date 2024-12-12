package perso.caupanharm.backend.models.caupanharm.valorant.analysis

data class CompStatsResponse(
    val settings: CompStatsSettings,
    val data: List<CompStats>
)

data class CompStatsSettings(
    val map: String?,
    val agents: List<String>,
    val sortType: String,
    val confidence: Int,
    val minCount: Int,
    val totalMatches: Int,
    val totalCompsFound: Int,
    val differentCompsFound: Int,
)

data class CompStats(
    val comp: List<String>,
    val bayesianAverage: Double,
    val count: Int,
    val winRate: Double
)

data class CompStatsCount(
    var count: Int,
    var wins: Int
)