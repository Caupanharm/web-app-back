package perso.caupanharm.backend.controllers

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import perso.caupanharm.backend.repositories.MatchRepository
import perso.caupanharm.backend.services.HenrikService
import perso.caupanharm.backend.services.LocalDataService
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponse
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponseType
import perso.caupanharm.backend.models.localdata.AdditionalCustomPlayerData
import perso.caupanharm.backend.models.localdata.BracketMatchData
import perso.caupanharm.backend.models.localdata.PlayersMatchData
import perso.caupanharm.backend.models.caupanharm.valorant.match.full.CaupanharmMatchFull
import perso.caupanharm.backend.models.caupanharm.valorant.matches.CaupanharmMatches
import perso.caupanharm.backend.transformers.FullMatchTransformer
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api")
class CaupanharmController(
    private val localDataService: LocalDataService,
    private val henrikService: HenrikService,
    private val fullMatchTransformer: FullMatchTransformer

) {
    private val logger = KotlinLogging.logger {}

    @Autowired
    lateinit var repository: MatchRepository

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

    @GetMapping("/stats")
    fun getStats(): List<AdditionalCustomPlayerData> {
        logger.info("Endpoint fetched: stats")
        return localDataService.getAdditionalPlayerData()
    }

    @GetMapping("/player")
    fun getPlayer(@RequestParam username: String): Any {
        logger.info("Endpoint fetched: player with params: username=${username}")
        val splittedName = username.split('#')
        return henrikService.getPlayerFromName(splittedName[0], splittedName[1])

    }

    @GetMapping("matches")
    fun getStoredMatches(@RequestParam username: String): Mono<CaupanharmResponse> {
        logger.info("Endpoint fetched: matches with params: username=${username}")
        val splittedName = username.split('#')

        return henrikService.getStoredMatches(splittedName[0], splittedName[1])
            .flatMap { response ->
                val caupanharmMatches: MutableList<CaupanharmMatchFull> = repository.findByPlayerName(username)
                    .map { it.toCaupanharmMatchFull() }
                    .toMutableList()
                logger.info(response.toString()                                                          )
                if (response.bodyType == CaupanharmResponseType.MATCH_HISTORY) {
                    val caupanharmMatchesIds: List<String> = caupanharmMatches.map { it.metadata.id }
                    val allMatchesIds = (response.body as CaupanharmMatches).data.map { it.metadata.id }
                    val missingMatchesIds = allMatchesIds.subtract(caupanharmMatchesIds.toSet())
                    var matchesAdded = 0
                    logger.info("Matches found: ${allMatchesIds.size}")
                    logger.info("Matches stored: ${caupanharmMatchesIds.size}")

                    // Utiliser un Flux pour récupérer tous les matchs manquants
                    return@flatMap Flux.fromIterable(missingMatchesIds)
                        .index()
                        .flatMap { tuple ->
                            val index = tuple.t1
                            val matchId = tuple.t2
                            henrikService.getMatchFromIdV4(matchId, missingMatchesIds.size, index.toInt() + 1)
                                .filter { it.bodyType == CaupanharmResponseType.MATCH_FULL }
                                .map { it.body as CaupanharmMatchFull }
                                .doOnNext { match ->
                                    try{
                                        repository.save(match.toPostgresMatch())
                                        matchesAdded++
                                        caupanharmMatches.add(match)
                                    }catch (e: Exception){
                                        logger.error(e.stackTraceToString())
                                    }
                                }
                        }
                        .collectList() // Collecte tous les résultats dans une liste
                        .flatMap { // Utiliser flatMap pour revenir à un Mono<CaupanharmResponse>
                            Mono.just(CaupanharmResponse(200, "Added $matchesAdded matches to the database. ${missingMatchesIds.size - matchesAdded} matches are still missing due to rate limits. If matches are missing, try again in a minute.", CaupanharmResponseType.MATCH_HISTORY, caupanharmMatches))
                        }
                } else {
                    // Si pas de MATCH_HISTORY, retourner la liste de départ dans un Mono
                    return@flatMap Mono.just(CaupanharmResponse(500, "API Rate exceeded. Please try again in a minute.", CaupanharmResponseType.MATCH_HISTORY, caupanharmMatches))
                }
            }
    }


    @GetMapping("/match")
    fun getMatch(@RequestParam("id") matchId: String): Mono<CaupanharmResponse> {
        logger.info("Endpoint fetched: match with params: matchId=${matchId}")
        try {
            val isInDb = false
            if (isInDb) {
                val dbData = null
                return Mono.just(
                    CaupanharmResponse(
                        200,
                        null,
                        CaupanharmResponseType.EXCEPTION,
                        dbData
                    )
                ) // TODO récupérer données db
            } else {
                return henrikService.getMatchFromIdV4(matchId).map { response ->
                    if (response.statusCode == 200) {
                        repository.save((response.body as CaupanharmMatchFull).toPostgresMatch())
                        response
                    } else {
                        response
                    }
                }
            }
        } catch (e: Exception) {
            return Mono.just(
                CaupanharmResponse(
                    500,
                    null,
                    CaupanharmResponseType.EXCEPTION,
                    e.toString()
                )
            )
        }
    }

    @GetMapping("analysis")
    fun getAnalysedMatch(
        @RequestParam("player") player: String,
        @RequestParam("id") matchId: String
    ): Mono<CaupanharmResponse> {
        logger.info("Endpoint fetched: analysis with params: matchId=${matchId}")
        val match = henrikService.getMatchFromIdV4(matchId)
        return match.flatMap { response ->
            if (response.statusCode == 200) {
                fullMatchTransformer.analyseFullMatch(player, response.body as CaupanharmMatchFull)
            } else {
                Mono.just(response)
            }
        }
    }

}