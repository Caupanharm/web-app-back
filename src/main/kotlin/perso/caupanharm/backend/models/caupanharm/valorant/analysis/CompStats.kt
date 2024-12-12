package perso.caupanharm.backend.models.caupanharm.valorant.analysis

data class CompStatsResponse(
    val settings: CompStatsSettings,
    val requestedAgentsStats: RequestedAgentsStats,
    val matchingComps: List<CompStats>
)

data class CompStatsSettings(
    val map: String?,
    val requestedAgents: List<String>,
    val sortType: String,
    val confidence: Int,
    val minGamesToBeCounted: Int
)

data class RequestedAgentsStats(
    val timesPlayed: Int,
    val differentCompsFound: Int,
    val pickRateInTeam: Double,
    val winRate: Double
)

data class CompStats(
    val comp: List<String>,
    val bayesianAverage: Double,
    val timesPlayed: Int,
    val pickRateInAllGames: Double,
    val pickRateInMatchingComps: Double,
    val winRate: Double
)

data class CompStatsCount(
    var count: Int,
    var wins: Int
)