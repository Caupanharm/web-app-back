package perso.caupanharm.backend.models.caupanharm.valorant.analysis

data class MatchesAgentsAnalysis(
    val globalTeamCount: Int,
    val allyTeamCount: Int?,
    val enemyTeamCount: Int?,
    val globalWinCount: Int?,
    val allyWinCount: Int?,
    val enemyWinCount: Int?,
    val globalWinrate: Double?,
    val allyWinrate: Double?,
    val enemyWinrate: Double?,
    val globalWinDelta: Double?,
    val globalLossDelta: Double?,
    val allyWinDelta: Double?,
    val enemyWinDelta: Double?
)