package perso.caupanharm.backend.models.henrik.v3

import perso.caupanharm.backend.models.henrik.V1LifetimeMatchesItem
import perso.caupanharm.backend.models.henrik.Results

data class Henrik3MatchesV1(
    val status: Int,
    val results: Results,
    val data: List<V1LifetimeMatchesItem>
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



