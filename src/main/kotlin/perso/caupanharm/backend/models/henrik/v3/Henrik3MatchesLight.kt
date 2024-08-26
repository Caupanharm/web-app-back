package perso.caupanharm.backend.models.henrik.v3

import perso.caupanharm.backend.models.henrik.HenrikV1LifetimeMatchesItem
import perso.caupanharm.backend.models.henrik.HenrikResults

data class Henrik3MatchesV1(
    val status: Int,
    val results: HenrikResults,
    val data: List<HenrikV1LifetimeMatchesItem>
)





data class MapInfo(
    val id: String,
    val name: String
)

data class Season(
    val id: String,
    val short: String
)

data class Character(
    val id: String,
    val name: String
)

data class Shots(
    val head: Int,
    val body: Int,
    val leg: Int
)



