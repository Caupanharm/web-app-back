package perso.caupanharm.backend.models.caupanharm.valorant.analysis

data class MapStats(
    val name: String,
    val occurences: Long,
    val atkWinrate: Double,
    val defWinrate: Double,
    val blueAttackWinrate: Double,
    val blueDefenseWinrate: Double,
    val redAttackWinrate: Double,
    val redDefenseWinrate: Double
)
