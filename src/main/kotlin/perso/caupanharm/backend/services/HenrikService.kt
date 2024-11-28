package perso.caupanharm.backend.services

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponse
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponseType
import perso.caupanharm.backend.models.caupanharm.valorant.account.HenrikAccount
import perso.caupanharm.backend.models.henrik.HenrikErrors
import perso.caupanharm.backend.models.caupanharm.valorant.match.full.HenrikMatchFull
import perso.caupanharm.backend.models.caupanharm.valorant.matches.HenrikMatches
import perso.caupanharm.backend.models.caupanharm.valorant.raw.RawMatchHistory
import reactor.core.publisher.Mono

@Service
class HenrikService(private val henrikClient: WebClient) {
    private val logger = KotlinLogging.logger {}

    @Value("\${valorant.season.current}")
    lateinit var currentValSeason: String

    fun getPlayerFromName(username: String, tag: String): Mono<CaupanharmResponse> {
        logger.info("GET  /valorant/v2/account/$username/$tag?force=true")
        return try {
            henrikClient.get()
                .uri("/valorant/v2/account/$username/$tag?force=true")
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


    fun getStoredMatches(name: String, tag: String): Mono<CaupanharmResponse> {
        logger.info("GET  /valorant/v1/stored-matches/eu/$name/$tag?mode=competitive")
        try {
            return henrikClient.get()
                .uri("valorant/v1/stored-matches/eu/$name/$tag?mode=competitive")
                .exchangeToMono { response ->
                    when (response.statusCode().value()) {
                        in 200..299 -> response.bodyToMono(HenrikMatches::class.java)
                            .map { matches ->
                                logger.info("Found ${matches.data.size} matches")
                                // Filtrer la liste data pour ne garder que les éléments de la saison en cours
                                val filteredData = matches.data.filter { it.meta.season.short == currentValSeason }
                                logger.info("${filteredData.size} of which are from season $currentValSeason")
                                matches.copy(data = filteredData).toCaupanharmResponse()
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
            return Mono.just(CaupanharmResponse(500, null, CaupanharmResponseType.EXCEPTION, body = e.toString()))
        }
    }

    fun getMatchFromIdV4(matchId: String, total: Int? = null, current: Int? = null): Mono<CaupanharmResponse> {
        if ((current == null) || (total == null)) {
            logger.info("GET  /valorant/v4/match/eu/$matchId")
        } else {
            logger.info("GET  /valorant/v4/match/eu/$matchId ($current/$total)")
        }
        try {
            return henrikClient.get()
                .uri("https://api.henrikdev.xyz/valorant/v4/match/eu/$matchId")
                .exchangeToMono { response ->
                    when (response.statusCode().value()) {
                        in 200..299 -> response.bodyToMono(HenrikMatchFull::class.java)
                            .map { match ->
                                CaupanharmResponse(
                                    200,
                                    null,
                                    CaupanharmResponseType.MATCH_FULL,
                                    match.toCaupanharmMatchFull()
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

    fun getRawHistory(uuid: String, startIndex: Int? = 0, endIndex: Int? = 20, queue: String? = "competitive"): Mono<CaupanharmResponse> {
        val bodyMap: Map<String, String> = mapOf(
            Pair("type", "matchhistory"),
            Pair("value", uuid),
            Pair("region", "eu"),
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

}