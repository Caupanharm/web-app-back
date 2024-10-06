package perso.caupanharm.backend.models.valorant.match.light

data class CaupanharmMatchLight(
    val metadata: CaupanharmMatchLightMetadata,
    val stats: CaupanharmMatchLightStats,
    val formattedStats: CaupanharmMatchLightFormatted
)

data class CaupanharmMatchLightMetadata(
    val id: String,
    val map: String,
    val startTime: String,
    val season: String,
    val region: String,
    val cluster: String
)

data class CaupanharmMatchLightStats(
    val team: String,
    val allyScore: Int,
    val enemyScore: Int,
    val agent: String,
    val tier: Int,
    val combatScore: Int,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val headshots: Int,
    val bodyshots: Int,
    val legshots: Int,
    val damageDealt: Int,
    val damageReceived: Int
)

data class CaupanharmMatchLightFormatted(
    val gameIssue: String,
    val kda: String,
    val kd: Double,
    val dd: Int,
    val hsp: Int,
    val bsp: Int,
    val lsp: Int,
    val adr: Int,
    val acs: Int
)
