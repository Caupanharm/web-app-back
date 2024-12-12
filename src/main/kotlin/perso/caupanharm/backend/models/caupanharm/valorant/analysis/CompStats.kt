package perso.caupanharm.backend.models.caupanharm.valorant.analysis

data class CompStatsResponse(
    val settings: CompStatsSettings,
    val requestedAgentsStats: RequestedAgentsStats,
    val comps: List<CompStats>
)

data class CompStatsSettings(
    val map: String?,
    val agents: List<String>,
    val sortType: String,
    val confidence: Int,
    val minCount: Int
)

data class RequestedAgentsStats(
    val totalCompsFound: Int,
    val differentCompsFound: Int,
    val pickRate: Double,
    val winRate: Double
)

data class CompStats(
    val comp: List<String>,
    val bayesianAverage: Double,
    val count: Int,
    val globalPickRate: Double,
    val localPickRate: Double,
    val winRate: Double
)

data class CompStatsCount(
    var count: Int,
    var wins: Int
)