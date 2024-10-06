package perso.caupanharm.backend.models.valorant.matches

import perso.caupanharm.backend.models.caupanharm.CaupanharmResponse
import perso.caupanharm.backend.models.valorant.match.light.HenrikMatchLight

// /valorant/v1/stored-matches/{region}/{name}/{tag}
data class HenrikMatches(
    val status: Int,
    val results: Results,
    val data: List<HenrikMatchLight>
){
    fun toCaupanharmMatches(): CaupanharmMatches{
        return CaupanharmMatches(
            dataInitialSize = data.size,
            data = data.map { match -> match.toCaupanharmMatchLight() }
        )
    }

    fun toCaupanharmResponse(): CaupanharmResponse {
        try {
            val caupanharmMatches = toCaupanharmMatches()
            return CaupanharmResponse(200, null, bodyType = "matches", caupanharmMatches)
        } catch (e: Exception) {
            return CaupanharmResponse(500, null, bodyType = "exception", e)
        }
    }
}

data class Results(
    val total: Int,
    val returned: Int,
    val before: Int,
    val after: Int
)

