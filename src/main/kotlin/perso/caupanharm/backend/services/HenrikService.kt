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

    @Value("\${valorant.season.current}")
    lateinit var currentValSeason: String

    fun getPlayerFromName(username: String): Mono<CaupanharmResponse> {
        val splittedName = username.split('#')
        if (splittedName.size != 2) return Mono.just(
            CaupanharmResponse(
                500,
                "Player not found",
                CaupanharmResponseType.EXCEPTION,
                "Requested player: $username"
            )
        )

        logger.info("GET  /valorant/v2/account/${splittedName[0]}/${splittedName[1]}?force=true")
        return try {
            henrikClient.get()
                .uri("/valorant/v2/account/${splittedName[0]}/${splittedName[1]}?force=true")
                .exchangeToMono { response ->
                    when (response.statusCode().value()) {
                        in 200..299 -> response.bodyToMono(HenrikAccount::class.java)
                            .map { henrikAccount -> henrikAccount.toCaupanharmResponse() }

                        else -> response.bodyToMono(HenrikErrors::class.java)
                            .map { henrikErrors ->
                                CaupanharmResponse(
                                    502,
                                    null,
                                    CaupanharmResponseType.EXCEPTION,
                                    henrikErrors
                                )
                            }
                    }
                }
        } catch (e: Exception) {
            Mono.just(CaupanharmResponse(500, null, CaupanharmResponseType.EXCEPTION, body = e.toString()))
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
                                CaupanharmResponse(
                                    200,
                                    null,
                                    CaupanharmResponseType.RAW_MATCH_HISTORY,
                                    rawMatchHistory
                                )
                            }

                        else -> response.bodyToMono(HenrikErrors::class.java)
                            .map { henrikErrors ->
                                CaupanharmResponse(
                                    502,
                                    null,
                                    CaupanharmResponseType.EXCEPTION,
                                    henrikErrors
                                )
                            }
                    }
                }

        } catch (e: Exception) {
            return Mono.just(CaupanharmResponse(500, null, CaupanharmResponseType.EXCEPTION, e.toString()))
        }
    }

    fun getRawMatch(uuid: String, region: String): Mono<String> {
        val bodyMap: Map<String, String> = mapOf(
            Pair("type", "matchdetails"),
            Pair("value", uuid),
            Pair("region", region),
            Pair("queries", "")
        )
        logger.info("POST https://api.henrikdev.xyz/valorant/v1/raw with body: $bodyMap")
        return henrikClient.post()
            .uri("https://api.henrikdev.xyz/valorant/v1/raw")
            .body(BodyInserters.fromValue(bodyMap))
            .exchangeToMono { response ->
                response.bodyToMono(String::class.java)
            }
    }


    fun getMatch(uuid: String, region: String): Mono<CaupanharmResponse> {
        val bodyMap: Map<String, String> = mapOf(
            Pair("type", "matchdetails"),
            Pair("value", uuid),
            Pair("region", region),
            Pair("queries", "")
        )
        logger.info("POST https://api.henrikdev.xyz/valorant/v1/raw with body: $bodyMap")
        return try {
            henrikClient.post()
                .uri("https://api.henrikdev.xyz/valorant/v1/raw")
                .body(BodyInserters.fromValue(bodyMap))
                .exchangeToMono { response ->
                    when (response.statusCode().value()) {
                        in 200..299 -> response.bodyToMono(RiotMatchFull::class.java)
                            .map { match ->
                                CaupanharmResponse(
                                    200,
                                    null,
                                    CaupanharmResponseType.RAW_MATCH,
                                    match
                                )
                            }

                        else -> response.bodyToMono(HenrikErrors::class.java)
                            .map { henrikErrors ->
                                CaupanharmResponse(
                                    502,
                                    null,
                                    CaupanharmResponseType.EXCEPTION,
                                    henrikErrors
                                )
                            }
                    }
                }
        } catch (e: Exception) {
            Mono.just(CaupanharmResponse(500, null, CaupanharmResponseType.EXCEPTION, e.toString()))
        }
    }
}