package perso.caupanharm.backend.models.caupanharm.valorant.raw

import com.fasterxml.jackson.annotation.JsonProperty

data class RawMatchHistory(
    @JsonProperty("Subject") val subject: String,
    @JsonProperty("BeginIndex") val startIndex: Int? = 0,
    @JsonProperty("EndIndex") val endIndex: Int? = 20,
    @JsonProperty("Total") val total: Int,
    @JsonProperty("History") val history: List<RawMatch>

)

data class RawMatch(
    @JsonProperty("MatchID") val id: String,
    @JsonProperty("GameStartTime") val startTime: Long,
    @JsonProperty("QueueID") val queue: String // todo: replace this by an enum listing all possible queues
)


