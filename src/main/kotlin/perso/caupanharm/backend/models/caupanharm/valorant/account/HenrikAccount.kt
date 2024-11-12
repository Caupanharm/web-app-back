package perso.caupanharm.backend.models.caupanharm.valorant.account

import perso.caupanharm.backend.models.caupanharm.CaupanharmResponse
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponseType

// /valorant/v2/account/{name}/{tag}
data class HenrikAccount(
    val status: Int,
    val data: HenrikAccountData
) {
    fun toCaupanharmResponse(): CaupanharmResponse {
        try {
            val caupanharmPlayer = CaupanharmPlayer(
                puuid = data.puuid,
                name = "${data.name}#${data.tag}",
                region = data.region,
                accountLevel = data.account_level,
                card = data.card,
                title = data.title,
                lastUpdate = data.updated_at
            )
            return CaupanharmResponse(200, null, CaupanharmResponseType.PLAYER, caupanharmPlayer)
        } catch (e: Exception) {
            return CaupanharmResponse(500, null, CaupanharmResponseType.EXCEPTION, e)
        }
    }
}

data class HenrikAccountData(
    val puuid: String,
    val region: String,
    val account_level: Int,
    val name: String,
    val tag: String,
    val card: String,
    val title: String,
    val platforms: List<String>,
    val updated_at: String
)
