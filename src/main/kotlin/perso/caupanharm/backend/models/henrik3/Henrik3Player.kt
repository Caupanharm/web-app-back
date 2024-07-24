package perso.caupanharm.backend.models.henrik3

data class Henrik3Player(
    val status: Int,
    val data: PlayerData
)

data class PlayerData(
    val puuid: String,
    val region: String,
    val account_level: Int,
    val name: String,
    val tag: String,
    val card: PlayerCard,
    val last_update: String,
    val last_update_raw: Long
)