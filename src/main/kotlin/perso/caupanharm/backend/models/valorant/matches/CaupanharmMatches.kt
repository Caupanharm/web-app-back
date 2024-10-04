package perso.caupanharm.backend.models.valorant.matches

import perso.caupanharm.backend.models.valorant.match.light.CaupanharmMatchLight

// Only tracks competitive matches so far, adapted to unrated matches as well (no other side modes)
data class CaupanharmMatches(
    val dataInitialSize: Int,
    val data: List<CaupanharmMatchLight>
)