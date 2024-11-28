package perso.caupanharm.backend.models.caupanharm.valorant.matches

import perso.caupanharm.backend.models.caupanharm.valorant.match.full.CaupanharmMatchFull

// Only tracks competitive matches so far, adapted to unrated matches as well (no other side modes)
data class CaupanharmMatchHistoryFull(
    val missingMatches: Int,
    val matches: List<CaupanharmMatchFull>
)
