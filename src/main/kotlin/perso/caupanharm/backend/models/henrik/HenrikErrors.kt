package perso.caupanharm.backend.models.henrik

data class HenrikErrors(
    val errors: List<Error>
)

data class Error(
    val code: Int,
    val message: String,
    val status: Int,
    val details: String?
)