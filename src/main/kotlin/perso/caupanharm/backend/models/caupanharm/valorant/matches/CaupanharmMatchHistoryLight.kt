package perso.caupanharm.backend.models.caupanharm.valorant.matches

import perso.caupanharm.backend.models.caupanharm.valorant.match.CaupanharmMatchLight

// Only tracks competitive matches so far, adapted to unrated matches as well (no other side modes)
data class CaupanharmMatchHistoryLight(
    val data: List<CaupanharmMatchLight>
)