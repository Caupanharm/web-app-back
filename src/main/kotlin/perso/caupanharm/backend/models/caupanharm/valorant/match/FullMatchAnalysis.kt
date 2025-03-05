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
    val clutchWinRate: Double?,
    val clutchSituationsByEnemiesAlive: List<Int>,
    val wonClutchesByEnemiesAlive: List<Int>,
) : Analyzable {
    override val computedScore: Double
        get() = calculateScore()

    private fun calculateScore(): Double {
        return 0.0 // TODO("Not yet implemented")
    }

    operator fun plus(other: ImpactAnalysis): ImpactAnalysis {
        return ImpactAnalysis(
            rounds = this.rounds + other.rounds,
            totalKills = this.totalKills + other.totalKills,
            totalDeaths = this.totalDeaths + other.totalDeaths,
            kd = if (this.totalDeaths + other.totalDeaths == 0) (this.totalKills + other.totalKills).toDouble() else (this.totalKills + other.totalKills).toDouble() / (this.totalDeaths + other.totalDeaths),
            totalScore = this.totalScore + other.totalScore,
            acs = ((this.totalScore + other.totalScore).toDouble() / (this.rounds + other.rounds)).roundToInt(),
            kastRounds = this.kastRounds + other.kastRounds,
            averageKAST = (this.kastRounds + other.kastRounds).toDouble() / (this.rounds + other.rounds),
            clutchWinRate = if (this.clutchSituationsByEnemiesAlive.sum() + other.clutchSituationsByEnemiesAlive.sum() == 0) null else (this.wonClutchesByEnemiesAlive.sum() + other.wonClutchesByEnemiesAlive.sum()).toDouble() / (this.clutchSituationsByEnemiesAlive.sum() + other.clutchSituationsByEnemiesAlive.sum()),
            clutchSituationsByEnemiesAlive = this.clutchSituationsByEnemiesAlive.zip(other.clutchSituationsByEnemiesAlive) { thisValue, otherValue -> thisValue + otherValue },
            wonClutchesByEnemiesAlive = this.wonClutchesByEnemiesAlive.zip(other.wonClutchesByEnemiesAlive) { thisValue, otherValue -> thisValue + otherValue }
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