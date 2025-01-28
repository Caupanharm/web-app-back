package perso.caupanharm.backend.controllers

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import perso.caupanharm.backend.services.HenrikService
import perso.caupanharm.backend.services.LocalDataService
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponse
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponseType
import perso.caupanharm.backend.models.caupanharm.valorant.account.CaupanharmPlayer
import perso.caupanharm.backend.models.localdata.AdditionalCustomPlayerData
import perso.caupanharm.backend.models.localdata.BracketMatchData
import perso.caupanharm.backend.models.localdata.PlayersMatchData
import perso.caupanharm.backend.models.caupanharm.valorant.match.CaupanharmMatchFull
import perso.caupanharm.backend.models.riot.RiotMatchFull
import perso.caupanharm.backend.models.caupanharm.valorant.matches.CaupanharmMatchHistoryFull
import perso.caupanharm.backend.models.riot.RawMatchHistory
import perso.caupanharm.backend.repositories.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api")
class CaupanharmController(
    private val localDataService: LocalDataService,
    private val henrikService: HenrikService,
    private val usernameRegex: Regex = "^[^,;*\\-#]+#[^,;*\\-#]+\$".toRegex(),
    private val alphaNumericalRegex: Regex = "^[a-zA-Z0-9]+$".toRegex()
) {
    @Autowired
    lateinit var matchXSRepository: MatchXSRepository

    @Autowired
    lateinit var matchXSAgentRepository: MatchXSAgentRepository

    // Placeholder data - to delete someday
    @GetMapping("/bracket")
    fun getBracket(): List<BracketMatchData> {
        logger.info("Endpoint fetched: bracket")
        return localDataService.getBracketData()
    }

    @GetMapping("/players")
    fun getPlayers(): List<PlayersMatchData> {
        logger.info("Endpoint fetched: players")
        return localDataService.getPlayersMatchesData()
    }

    @GetMapping("/playerStats")
    fun getStats(): List<AdditionalCustomPlayerData> {
        logger.info("Endpoint fetched: stats")
        return localDataService.getAdditionalPlayerData()
    }

    // Actual data
    @GetMapping("/player")
    fun getPlayer(@RequestParam username: String): Mono<CaupanharmResponse> {
        logger.info("Endpoint fetched: player with params: username=${username}")
        return if (!usernameRegex.matches(username)) {
            Mono.just(CaupanharmResponse(500, "Invalid parameters", CaupanharmResponseType.EXCEPTION, null))
        } else {
            henrikService.getPlayerFromName(username)
        }

    }

    @GetMapping("/matches")
    fun getRawHistory(@RequestParam username: String, region: String = "eu", queue: String = "competitive", start: Int? = 0, end: Int? = 20): Mono<CaupanharmResponse> {
        logger.info("Endpoint fetched: rawHistory with params: username=$username, region=$region, queue=$queue")
        if (!usernameRegex.matches(username) || !alphaNumericalRegex.matches(region) || !alphaNumericalRegex.matches(
                queue
            )
        ) {
            return Mono.just(CaupanharmResponse(500, "Invalid parameters", CaupanharmResponseType.EXCEPTION, null))
        }

        return henrikService.getPlayerFromName(username)
            .flatMap { playerResponse ->
                if (playerResponse.statusCode == 200) {
                    val puuid = (playerResponse.body as CaupanharmPlayer).puuid
                    henrikService.getHistory(puuid, region, queue, start, end)
                        .flatMap { rawHistoryResponse ->
                            val caupanharmMatches: MutableList<CaupanharmMatchFull> = mutableListOf()

                            if (rawHistoryResponse.bodyType == CaupanharmResponseType.RAW_MATCH_HISTORY) {
                                val caupanharmMatchesIds: List<String> = caupanharmMatches.map { it.metadata.matchId }
                                val allMatchesIds =
                                    (rawHistoryResponse.body as RawMatchHistory).history.map { it.matchId }
                                val missingMatchesIds = allMatchesIds.subtract(caupanharmMatchesIds.toSet())
                                var savedMatches = caupanharmMatchesIds.size
                                var matchesAdded = 0
                                logger.info("Matches found: ${rawHistoryResponse.body.total}")
                                logger.info("Matches initially stored: ${caupanharmMatchesIds.size}")

                                // Flux pour récupérer tous les matchs manquants
                                return@flatMap Flux.fromIterable(missingMatchesIds)
                                    .flatMap { matchId ->
                                        henrikService.getMatch(matchId, region)
                                            .filter { it.bodyType == CaupanharmResponseType.RAW_MATCH }
                                            .map { it.body as RiotMatchFull }
                                            .doOnNext { match ->
                                                try {
                                                    val caupanharmMatchFull = match.toCaupanharmMatchFull()
                                                    matchesAdded++
                                                    savedMatches++
                                                    caupanharmMatches.add(caupanharmMatchFull)
                                                } catch (e: Exception) {
                                                    logger.error(e.stackTraceToString())
                                                }
                                            }
                                    }
                                    .collectList() // collect all the results in a list
                                    .flatMap { // flatMap pour revenir à un Mono<CaupanharmResponse>
                                        Mono.just(
                                            CaupanharmResponse(
                                                200,
                                                "Added $matchesAdded matches to the database. ${rawHistoryResponse.body.total - savedMatches} matches are still missing due to rate limits. If matches are missing, try again in a minute.",
                                                CaupanharmResponseType.MATCH_HISTORY,
                                                CaupanharmMatchHistoryFull(
                                                    rawHistoryResponse.body.total - savedMatches,
                                                    caupanharmMatches
                                                )
                                            )
                                        )
                                    }

                            } else {
                                // Si pas de RAW_MATCH_HISTORY, retourner la liste de départ dans un Mono
                                return@flatMap Mono.just(
                                    CaupanharmResponse(
                                        500,
                                        "API Rate exceeded. Please try again in a minute.",
                                        CaupanharmResponseType.EXCEPTION,
                                        caupanharmMatches
                                    )
                                )
                            }
                        }
                } else {
                    // Si la réponse de playerResponse n'est pas valide, on renvoie une erreur
                    return@flatMap Mono.just(
                        CaupanharmResponse(
                            500,
                            "Player not found or error occurred",
                            CaupanharmResponseType.EXCEPTION,
                            playerResponse.body
                        )
                    )
                }
            }
    }

    @GetMapping("/rawMatch")
    fun getRawMatch(@RequestParam("id") matchId: String, @RequestParam("region") region: String = "eu"): Mono<CaupanharmResponse> {
        logger.info("Endpoint fetched: rawMatch with params: matchId=${matchId}")
        if (!alphaNumericalRegex.matches(matchId) || !alphaNumericalRegex.matches(region)) {
            return Mono.just(CaupanharmResponse(500, "Invalid parameters", CaupanharmResponseType.EXCEPTION, null))
        }
        return henrikService.getRawMatch(matchId, region)
    }

    @GetMapping("/match")
    fun getMatch(@RequestParam("id") matchId: String, @RequestParam("queue") region: String = "eu"): Mono<CaupanharmResponse> {
        logger.info("Endpoint fetched: match with params: matchId=${matchId}")
        if (!alphaNumericalRegex.matches(matchId) || !alphaNumericalRegex.matches(region)) {
            return Mono.just(CaupanharmResponse(500, "Invalid parameters", CaupanharmResponseType.EXCEPTION, null))
        }
        return henrikService.getMatch(matchId, region)
            .map { response ->
                if (response.statusCode == 200) {
                    val match = (response.body as RiotMatchFull).toCaupanharmMatchFull()
                    CaupanharmResponse(200, null, CaupanharmResponseType.MATCH_FULL, match)
                } else {
                    response
                }
            }
    }

    @GetMapping("/matchXS")
    fun getMatchXS(@RequestParam("id") matchId: String, @RequestParam("queue") region: String = "eu"): Mono<CaupanharmResponse> {
        logger.info("Endpoint fetched: matchXS with params: matchId=${matchId}")
        if (!alphaNumericalRegex.matches(matchId) || !alphaNumericalRegex.matches(region)) {
            return Mono.just(CaupanharmResponse(500, "Invalid parameters", CaupanharmResponseType.EXCEPTION, null))
        }

        return henrikService.getMatch(matchId, region)
            .map { response ->
                if (response.statusCode == 200) {
                    val match = (response.body as RiotMatchFull).toCaupanharmMatchFull()
                    matchXSRepository.save(match.toPostgresMatchXS())
                    matchXSAgentRepository.saveAll(match.toPostgresMatchXSAgents())
                    CaupanharmResponse(200, null, CaupanharmResponseType.MATCH_XS, match.toPostgresMatchXSAgents())
                } else {
                    response
                }
            }
    }
}