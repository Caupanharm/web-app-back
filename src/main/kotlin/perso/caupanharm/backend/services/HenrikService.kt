package perso.caupanharm.backend.services

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponse
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponseType
import perso.caupanharm.backend.models.caupanharm.valorant.account.HenrikAccount
import perso.caupanharm.backend.models.henrik.HenrikErrors
import perso.caupanharm.backend.models.caupanharm.valorant.match.full.HenrikMatchFull
import perso.caupanharm.backend.models.caupanharm.valorant.matches.HenrikMatches
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
                            .map { henrikErrors -> CaupanharmResponse(502, null, CaupanharmResponseType.EXCEPTION, henrikErrors) }
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
                                // Filtrer la liste data pour ne garder que les éléments de la saison en cours
                                val filteredData = matches.data.filter { it.meta.season.short == currentValSeason }
                                matches.copy(data = filteredData).toCaupanharmResponse()
                            }

                        else -> response.bodyToMono(HenrikErrors::class.java)
                            .map { henrikErrors -> CaupanharmResponse(502, null, CaupanharmResponseType.EXCEPTION, henrikErrors) }
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
                            .map { henrikErrors -> CaupanharmResponse(502, null, CaupanharmResponseType.EXCEPTION, henrikErrors) }
                    }
                }
        } catch (e: Exception) {
            return Mono.just(CaupanharmResponse(500, null, CaupanharmResponseType.EXCEPTION, e.toString()))
        }


    }
}