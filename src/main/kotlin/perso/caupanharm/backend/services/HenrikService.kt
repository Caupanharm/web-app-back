package perso.caupanharm.backend.services

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import perso.caupanharm.backend.models.henrik.HenrikErrors
import perso.caupanharm.backend.models.henrik.HenrikMatchResponseV4
import perso.caupanharm.backend.models.henrik.V1LifetimeMatches
import perso.caupanharm.backend.models.henrik.v3.*
import reactor.core.publisher.Mono

@Service
class HenrikService(private val henrik3Client: WebClient) {
    private val logger = KotlinLogging.logger {}

    @Value("\${valorant.season.current}")
    lateinit var currentValSeason: String

    fun getPlayerFromName(username: String, tag: String): Mono<Any> {
        logger.info("GET  /valorant/v1/account/$username/$tag?force=true")
        return henrik3Client.get()
                            .uri("/valorant/v1/account/$username/$tag?force=true")
                            .exchangeToMono { response ->
                                when (response.statusCode().value()) {
                                    in 200..299 -> response.bodyToMono(Henrik3Player::class.java)
                                    else -> response.bodyToMono(HenrikErrors::class.java)
                                }
                            }
    }

    fun getMatchesLightFromUUID(uuid: String): Mono<Any> {
        logger.info("GET  /valorant/v1/by-puuid/lifetime/matches/eu/$uuid?mode=competitive")
        return henrik3Client.get()
                            .uri("/valorant/v1/by-puuid/lifetime/matches/eu/$uuid?mode=competitive")
                            .exchangeToMono { response ->
                                when (response.statusCode().value()) {
                                    in 200..299 -> response.bodyToMono(Henrik3MatchesV1::class.java)
                                    else -> response.bodyToMono(HenrikErrors::class.java)
                                }
                            }
    }

    // Doesn't fetch every match
    fun getMatchesFullFromUUID(uuid: String): Mono<Any> {
        logger.info("GET  /valorant/v3/matches/eu/$uuid?mode=competitive")
        return henrik3Client.get()
                            .uri("valorant/v3/by-puuid/matches/eu/$uuid?mode=competitive")
                            .exchangeToMono { response ->
                                when (response.statusCode().value()) {
                                    in 200..299 -> response.bodyToMono(Henrik3MatchesV3::class.java)
                                    else -> response.bodyToMono(HenrikErrors::class.java)
                                }
                            }
    }

    fun getMatchesLightFromName(name: String, tag: String): Mono<Any> {
        logger.info("GET  /valorant/v1/lifetime/matches/eu/$name/$tag?mode=competitive")
        return henrik3Client.get()
                            .uri("/valorant/v1/lifetime/matches/eu/$name/$tag?mode=competitive")
                            .exchangeToMono { response ->
                                when (response.statusCode().value()) {
                                    in 200..299 -> response.bodyToMono(V1LifetimeMatches::class.java)
                                    else -> response.bodyToMono(HenrikErrors::class.java)
                                }
                            }
    }

    fun getStoredMatches(name: String, tag: String): Mono<Any> {
        logger.info("GET  valorant/v1/stored-matches/eu/$name/$tag?mode=competitive")
        return henrik3Client.get()
            .uri("valorant/v1/stored-matches/eu/$name/$tag?mode=competitive")
            .exchangeToMono { response ->
                when (response.statusCode().value()) {
                    in 200..299 -> response.bodyToMono(V1LifetimeMatches::class.java)
                        .map { v1LifetimeMatches ->
                            // Filtrer la liste data pour ne garder que les éléments de la saison en cours
                            val originalDataSize = v1LifetimeMatches.data.size
                            val filteredData = v1LifetimeMatches.data.filter { it.meta.season.short == currentValSeason }
                            val filteredDataSize = filteredData.size

                            val eliminatedCount = originalDataSize - filteredDataSize

                            val updatedResults = v1LifetimeMatches.results.copy(
                                before = v1LifetimeMatches.results.total,
                                returned = v1LifetimeMatches.results.returned - eliminatedCount,
                                after = v1LifetimeMatches.results.after - eliminatedCount
                            )

                            // Retourner un nouveau V1LifetimeMatches avec la liste filtrée et les résultats mis à jour
                            v1LifetimeMatches.copy(data = filteredData, results = updatedResults)
                        }
                    else -> response.bodyToMono(HenrikErrors::class.java)
                }
            }
    }

    fun getMatchFromIdV2(matchId: String, total: Int?, current: Int?): Mono<Any> {
        if((current == null) || (total == null)){
            logger.info("GET  /valorant/v2/match/$matchId")
        }else{
            logger.info("GET  /valorant/v2/match/$matchId ($current/$total)")
        }
        return henrik3Client.get()
                            .uri("/valorant/v2/match/$matchId")
                            .exchangeToMono { response ->
                                when (response.statusCode().value()) {
                                    in 200..299 -> response.bodyToMono(Henrik3MatchV3::class.java)
                                    else -> response.bodyToMono(HenrikErrors::class.java)
                                }
                            }
    }

    fun getMatchFromIdV4(matchId: String, total: Int? = null, current: Int? = null): Mono<Any> {
        if((current == null) || (total == null)){
            logger.info("GET  https://api.henrikdev.xyz/valorant/v4/match/eu/$matchId")
        }else{
            logger.info("GET  https://api.henrikdev.xyz/valorant/v4/match/eu/$matchId ($current/$total)")
        }
        return henrik3Client.get()
            .uri("https://api.henrikdev.xyz/valorant/v4/match/eu/$matchId")
            .exchangeToMono { response ->
                when (response.statusCode().value()) {
                    in 200..299 -> response.bodyToMono(HenrikMatchResponseV4::class.java)
                    else -> response.bodyToMono(HenrikErrors::class.java)
                }
            }
    }


}