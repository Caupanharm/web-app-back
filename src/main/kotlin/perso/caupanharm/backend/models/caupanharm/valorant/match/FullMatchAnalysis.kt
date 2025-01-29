package perso.caupanharm.backend.models.caupanharm.valorant.match

data class FullMatchAnalysis(
    val attackRounds: List<CaupanharmMatchRound>,
    val defenseRounds: List<CaupanharmMatchRound>
)
