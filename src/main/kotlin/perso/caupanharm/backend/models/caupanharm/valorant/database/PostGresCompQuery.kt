package perso.caupanharm.backend.models.caupanharm.valorant.database

import jakarta.persistence.Column

data class PostGresCompQuery(
    val map: String,
    val allyScore: Int,
    val enemyScore: Int,
    val attackScore: Int,
    val defenseScore: Int,
    val teamAgents: List<String>
)