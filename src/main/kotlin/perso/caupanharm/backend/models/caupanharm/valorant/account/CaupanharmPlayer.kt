package perso.caupanharm.backend.models.caupanharm.valorant.account

data class CaupanharmPlayer(
    val puuid: String,
    val name: String,
    val region: String,
    val accountLevel: Int,
    val card: String,
    val title: String,
    val lastUpdate: String
)
