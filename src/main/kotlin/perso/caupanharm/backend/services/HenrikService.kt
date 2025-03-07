package perso.caupanharm.backend.services

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponse
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponseType
import perso.caupanharm.backend.models.caupanharm.valorant.account.HenrikAccount
import perso.caupanharm.backend.models.henrik.HenrikErrors
import perso.caupanharm.backend.models.riot.RiotMatchFull
import perso.caupanharm.backend.models.riot.RawMatchHistory
import reactor.core.publisher.Mono

@Service
class HenrikService(private val henrikClient: WebClient) {
    private val logger = KotlinLogging.logger {}

    @Value("\${valorant.current.season}")
    lateinit var currentValSeason: String

    fun getPlayerFromName(username: String): Mono<CaupanharmResponse> {
        val splittedName = username.split('#')
        if (splittedName.size != 2) {
            val caupanharmResponse = CaupanharmResponse(
                500,
                "Player not found",
                CaupanharmResponseType.EXCEPTION,
                "Requested player: $username"
            )
            return Mono.just(caupanharmResponse)
        }

        logger.info("GET  /valorant/v2/account/${splittedName[0]}/${splittedName[1]}?force=true")
        return try {
            henrikClient.get()
                .uri("/valorant/v2/account/${splittedName[0]}/${splittedName[1]}?force=true")
                .exchangeToMono { response ->
                    when (response.statusCode().value()) {
                        in 200..299 -> {
                            val caupanharmResponse = response.bodyToMono(HenrikAccount::class.java)
                                .map { henrikAccount -> henrikAccount.toCaupanharmResponse() }
                            caupanharmResponse

                        }

                        else -> {
                            val caupanharmResponse = response.bodyToMono(HenrikErrors::class.java)
                                .map { henrikErrors ->
                                    CaupanharmResponse(
                                        502,
                                        null,
                                        CaupanharmResponseType.EXCEPTION,
                                        henrikErrors
                                    )
                                }
                            caupanharmResponse
                        }
                    }
                }
        } catch (e: Exception) {
            val caupanharmResponse = CaupanharmResponse(500, null, CaupanharmResponseType.EXCEPTION, body = e.toString())
            Mono.just(caupanharmResponse)
        }
    }


    fun getHistory(
        uuid: String,
        region: String,
        queue: String,
        startIndex: Int? = 0,
        endIndex: Int? = 20
    ): Mono<CaupanharmResponse> {
        val bodyMap: Map<String, String> = mapOf(
            Pair("type", "matchhistory"),
            Pair("value", uuid),
            Pair("region", region),
            Pair("queries", "?startIndex=$startIndex&endIndex=$endIndex&queue=$queue")
        )
        logger.info("POST https://api.henrikdev.xyz/valorant/v1/raw with body: $bodyMap")

        try {
            return henrikClient.post()
                .uri("https://api.henrikdev.xyz/valorant/v1/raw")
                .body(BodyInserters.fromValue(bodyMap))
                .exchangeToMono { response ->
                    when (response.statusCode().value()) {
                        in 200..299 -> response.bodyToMono(RawMatchHistory::class.java)
                            .map { rawMatchHistory ->
                                val caupanharmResponse = CaupanharmResponse(
                                    200,
                                    null,
                                    CaupanharmResponseType.RAW_MATCH_HISTORY,
                                    rawMatchHistory
                                )
                                caupanharmResponse
                            }
                        else -> response.bodyToMono(HenrikErrors::class.java)
                            .map { henrikErrors ->
                                val caupanharmResponse = CaupanharmResponse(
                                    502,
                                    null,
                                    CaupanharmResponseType.EXCEPTION,
                                    henrikErrors
                                )
                                caupanharmResponse
                            }
                    }
                }

        } catch (e: Exception) {
            val caupanharmResponse = CaupanharmResponse(500, null, CaupanharmResponseType.EXCEPTION, e.toString())
            return Mono.just(caupanharmResponse)
        }
    }

    fun getRawMatch(uuid: String, region: String): Mono<CaupanharmResponse> {
        val bodyMap: Map<String, String> = mapOf(
            Pair("type", "matchdetails"),
            Pair("value", uuid),
            Pair("region", region),
            Pair("queries", "string")
        )
        logger.info("POST https://api.henrikdev.xyz/valorant/v1/raw with body: $bodyMap")
        return henrikClient.post()
            .uri("https://api.henrikdev.xyz/valorant/v1/raw")
            .body(BodyInserters.fromValue(bodyMap))
            .exchangeToMono { response ->
                response.bodyToMono(String::class.java).flatMap { body ->
                    Mono.just(CaupanharmResponse(200, null, CaupanharmResponseType.RAW_MATCH, body))
                }
            }
    }


    fun getMatch(uuid: String, region: String = "eu"): Mono<CaupanharmResponse> {
        val bodyMap: Map<String, String> = mapOf(
            Pair("type", "matchdetails"),
            Pair("value", uuid),
            Pair("region", region),
            Pair("queries", "")
        )
        logger.info("POST https://api.henrikdev.xyz/valorant/v1/raw with body: $bodyMap")
        try {
            return henrikClient.post()
                .uri("https://api.henrikdev.xyz/valorant/v1/raw")
                .body(BodyInserters.fromValue(bodyMap))
                .exchangeToMono { response ->
                    when (response.statusCode().value()) {
                        in 200..299 -> response.bodyToMono(RiotMatchFull::class.java)
                            .map { match ->
                                val caupanharmResponse = CaupanharmResponse(
                                    200,
                                    null,
                                    CaupanharmResponseType.RAW_MATCH,
                                    match
                                )
                                caupanharmResponse
                            }

                        else -> response.bodyToMono(String::class.java)
                            .map { error ->
                                val caupanharmResponse = CaupanharmResponse(
                                    502,
                                    null,
                                    CaupanharmResponseType.EXCEPTION,
                                    error
                                )
                                caupanharmResponse
                            }
                    }
                }
        } catch (e: Exception) {
            val caupanharmResponse = CaupanharmResponse(500, null, CaupanharmResponseType.EXCEPTION, e.toString())
            return Mono.just(caupanharmResponse)
        }
    }
}