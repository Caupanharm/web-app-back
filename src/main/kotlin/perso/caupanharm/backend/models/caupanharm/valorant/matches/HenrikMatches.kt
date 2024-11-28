package perso.caupanharm.backend.models.caupanharm.valorant.matches

import perso.caupanharm.backend.models.caupanharm.CaupanharmResponse
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponseType
import perso.caupanharm.backend.models.caupanharm.valorant.match.light.HenrikMatchLight

// /valorant/v1/stored-matches/{region}/{name}/{tag}
data class HenrikMatches(
    val status: Int,
    val results: Results,
    val data: List<HenrikMatchLight>
){
    fun toCaupanharmMatches(): CaupanharmMatchHistoryLight {
        return CaupanharmMatchHistoryLight(
            data = data.map { match -> match.toCaupanharmMatchLight() }
        )
    }

    fun toCaupanharmResponse(): CaupanharmResponse {
        try {
            val caupanharmMatches = toCaupanharmMatches()
            return CaupanharmResponse(200, null, CaupanharmResponseType.MATCH_HISTORY, caupanharmMatches)
        } catch (e: Exception) {
            return CaupanharmResponse(500, null, CaupanharmResponseType.EXCEPTION, e)
        }
    }
}

data class Results(
    val total: Int,
    val returned: Int,
    val before: Int,
    val after: Int
)

