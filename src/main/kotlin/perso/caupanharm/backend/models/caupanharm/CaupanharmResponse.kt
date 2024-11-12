package perso.caupanharm.backend.models.caupanharm

data class CaupanharmResponse(
    val statusCode: Int,
    val message: String?,
    val bodyType: CaupanharmResponseType,
    val body: Any?
)