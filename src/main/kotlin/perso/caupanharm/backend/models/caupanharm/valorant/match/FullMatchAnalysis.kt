package perso.caupanharm.backend.models.caupanharm.valorant.match

import kotlin.math.roundToInt

data class FullMatchAnalysis(
    val rounds: List<CaupanharmMatchRound>, //TODO(remove after testing)
    val global: SideAnalysis,
    val attack: SideAnalysis,
    val defense: SideAnalysis,
)

data class SideAnalysis(
    val impact: ImpactAnalysis,
    //val economy: EconomyAnalysis,
    //val execution: ExecutionAnalysis,
    //val strategy: StrategyAnalysis
)

interface Analyzable {
    val computedScore: Double
}

data class ImpactAnalysis(
    val rounds: Int,
    val totalKills: Int,
    val totalDeaths: Int,
    val kd: Double,
    val totalScore: Int,
    val acs: Int,
    val kastRounds: Int,
    val averageKAST: Double,
    val totalClutches: Int,
    val wonClutches: Int,
    val clutchWinRate: Double?,
    val totalDuels: Int,
    val wonDuels: Int,
    val duelWinRate: Double?,
): Analyzable{
    override val computedScore: Double
        get() = calculateScore()

        private fun calculateScore(): Double {
            return 0.0 // TODO("Not yet implemented")
        }

    operator fun plus(other: ImpactAnalysis): ImpactAnalysis{
        return ImpactAnalysis(
            this.rounds + other.rounds,
            this.totalKills + other.totalKills,
            this.totalDeaths + other.totalDeaths,
            if(this.totalDeaths + other.totalDeaths == 0) (this.totalKills + other.totalKills).toDouble() else (this.totalKills + other.totalKills).toDouble() / (this.totalDeaths + other.totalDeaths),
            this.totalScore + other.totalScore,
            ((this.totalScore + other.totalScore).toDouble() / (this.rounds + other.rounds)).roundToInt(),
            this.kastRounds + other.kastRounds,
            (this.kastRounds + other.kastRounds).toDouble() / (this.rounds + other.rounds),
            this.totalClutches + other.totalClutches,
            this.wonClutches + other.wonClutches,
            if(this.totalClutches + other.totalClutches == 0) null else (this.wonClutches + other.wonClutches).toDouble() / (this.totalClutches + other.totalClutches),
            this.totalDuels + other.totalDuels,
            this.wonDuels + other.wonDuels,
             if(this.totalDuels + other.totalDuels == 0) null else (this.wonDuels + other.wonDuels).toDouble() / (this.totalDuels + other.totalDuels)
        )
    }
}

data class EconomyAnalysis(
    val r1Econ: Boolean,
    val r2Econ: Boolean,
    val r3Econ: Boolean,
    val lastRoundEcon: Boolean,
    val teamEconMatching: Int
)

data class ExecutionAnalysis(
    val placeholder: Any,
)

data class StrategyAnalysis(
    val placeholder: Any,
)