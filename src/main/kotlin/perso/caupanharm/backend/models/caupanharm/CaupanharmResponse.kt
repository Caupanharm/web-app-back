package perso.caupanharm.backend.models.caupanharm

data class CaupanharmResponse(
    val statusCode: Int,
    val errorCode: Int?,
    val body: Any?
)