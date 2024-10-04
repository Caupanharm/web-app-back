package perso.caupanharm.backend.controllers

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import perso.caupanharm.backend.repositories.MatchRepository
import perso.caupanharm.backend.Utils
import perso.caupanharm.backend.services.CaupanharmService
import perso.caupanharm.backend.services.LocalDataService
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponse
import perso.caupanharm.backend.models.localdata.AdditionalCustomPlayerData
import perso.caupanharm.backend.models.localdata.BracketMatchData
import perso.caupanharm.backend.models.localdata.PlayersMatchData
import perso.caupanharm.backend.models.valorant.match.full.HenrikMatchFull
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api")
class DataController(private val localDataService: LocalDataService, private val caupanharmService: CaupanharmService) {
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

    @GetMapping("/player/{username}/{tag}")
    fun getPlayer(@PathVariable username: String, @PathVariable tag: String): Any {
        logger.info("Endpoint fetched: player")
        return try {
            caupanharmService.getPlayerFromName(username, tag)
        }catch(e: Exception){
            Mono.just(CaupanharmResponse(500, errorCode = null, bodyType = "exception", body = e))
        }
    }

    @GetMapping("/matches/{username}/{tag}")
    fun getStoredMatches(@PathVariable username: String, @PathVariable tag: String): Mono<CaupanharmResponse> {
        logger.info("Endpoint fetched: matches")
        return try {
            caupanharmService.getStoredMatches(username, tag)
        } catch (e: Exception) {
            Mono.just(CaupanharmResponse(500, errorCode = null, bodyType = "exception", body = e))
        }
    }

    @GetMapping("/match")
    fun getMatch(@RequestParam("id") matchId: String): Mono<CaupanharmResponse> {
        logger.info("Endpoint fetched: match")
        try {
            val isInDb = false
            if (isInDb) {
                val dbData = null
                return Mono.just(CaupanharmResponse(200, null, bodyType = "exception", dbData)) // TODO récupérer données db
            } else {
                return caupanharmService.getMatchFromIdV4(matchId).map { response ->
                    if (response.body is HenrikMatchFull) {
                        repository.save(Utils.caupanharmToPostgresMatch(response.body.toCaupanharmMatchFull())) // TODO passer le toPostGresMatch directement dans CaupanharmMatchFull
                        response
                    } else {
                        response
                    }
                }
            }
        } catch (e: Exception) {
            return Mono.just(CaupanharmResponse(500, errorCode = null, bodyType = "exception", body = e))
        }
    }

}