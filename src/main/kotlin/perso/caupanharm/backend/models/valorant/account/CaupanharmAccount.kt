package perso.caupanharm.backend.models.valorant.account

data class CaupanharmAccount(
    val puuid: String,
    val name: String,
    val region: String,
    val accountLevel: Int,
    val card: String,
    val title: String,
    val lastUpdate: String
)
