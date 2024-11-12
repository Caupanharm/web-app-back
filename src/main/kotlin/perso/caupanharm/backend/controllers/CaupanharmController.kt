package perso.caupanharm.backend.controllers

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import perso.caupanharm.backend.repositories.MatchRepository
import perso.caupanharm.backend.services.CaupanharmService
import perso.caupanharm.backend.services.LocalDataService
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponse
import perso.caupanharm.backend.models.localdata.AdditionalCustomPlayerData
import perso.caupanharm.backend.models.localdata.BracketMatchData
import perso.caupanharm.backend.models.localdata.PlayersMatchData
import perso.caupanharm.backend.models.caupanharm.valorant.match.full.CaupanharmMatchFull
import perso.caupanharm.backend.transformers.FullMatchTransformer
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api")
class CaupanharmController(
    private val localDataService: LocalDataService,
    private val caupanharmService: CaupanharmService,
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
        return caupanharmService.getPlayerFromName(splittedName[0], splittedName[1])

    }

    @GetMapping("/matches")
    fun getStoredMatches(@RequestParam username: String): Mono<CaupanharmResponse> {
        logger.info("Endpoint fetched: matches with params: username=${username}")
        val splittedName = username.split('#')
        return caupanharmService.getStoredMatches(splittedName[0], splittedName[1])
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
                        bodyType = "exception",
                        dbData
                    )
                ) // TODO récupérer données db
            } else {
                return caupanharmService.getMatchFromIdV4(matchId).map { response ->
                    if (response.statusCode == 200) {
                        repository.save((response.body as CaupanharmMatchFull).toPostgresMatch())
                        response
                    } else {
                        response
                    }
                }
            }
        } catch (e: Exception) {
            return Mono.just(CaupanharmResponse(500, errorCode = null, bodyType = "exception", body = e.toString()))
        }
    }

    @GetMapping("analysis")
    fun getAnalysedMatch(
        @RequestParam("player") player: String,
        @RequestParam("id") matchId: String
    ): Mono<CaupanharmResponse> {
        logger.info("Endpoint fetched: analysis with params: matchId=${matchId}")
        val match = caupanharmService.getMatchFromIdV4(matchId)
        return match.flatMap { response ->
            if (response.statusCode == 200) {
                fullMatchTransformer.analyseFullMatch(player, response.body as CaupanharmMatchFull)
            } else {
                Mono.just(response)
            }
        }
    }

}