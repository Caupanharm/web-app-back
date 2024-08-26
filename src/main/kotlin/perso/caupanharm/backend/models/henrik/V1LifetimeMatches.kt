package perso.caupanharm.backend.models.henrik

data class V1LifetimeMatches (
    val status: String,
    val name: String?,
    val tag: String?,
    val results: HenrikResults,
    val data: List<HenrikV1LifetimeMatchesItem>
)