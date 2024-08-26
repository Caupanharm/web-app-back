package perso.caupanharm.backend.models.caupanharm

data class CaupanharmResponse(
    val resolved: Boolean,
    val errorCode: Int?,
    val body: Any?
)