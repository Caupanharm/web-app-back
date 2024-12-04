package perso.caupanharm.backend.models.caupanharm

data class CaupanharmResponse(
    val statusCode: Int,
    val message: String?,
    val bodyType: CaupanharmResponseType,
    val body: Any?
) {
    override fun toString(): String {
        return "CaupanharmResponse(statusCode=$statusCode, message=$message, bodyType=$bodyType, body=$body)"
    }
}