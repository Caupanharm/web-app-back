package perso.caupanharm.backend.models.henrik

data class V1LifetimeMatches (
    val status: String,
    val name: String?,
    val tag: String?,
    val results: Results,
    val data: List<V1LifetimeMatchesItem>
)