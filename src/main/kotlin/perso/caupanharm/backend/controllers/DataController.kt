package perso.caupanharm.backend.controllers

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import perso.caupanharm.backend.repositories.MatchRepository
import perso.caupanharm.backend.Utils
import perso.caupanharm.backend.services.HenrikService
import perso.caupanharm.backend.services.LocalDataService
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponse
import perso.caupanharm.backend.models.henrik.HenrikErrors
import perso.caupanharm.backend.models.henrik.HenrikMatchResponseV4
import perso.caupanharm.backend.models.henrik.v3.Henrik3Player
import perso.caupanharm.backend.models.localdata.AdditionalCustomPlayerData
import perso.caupanharm.backend.models.localdata.BracketMatchData
import perso.caupanharm.backend.models.localdata.PlayersMatchData
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api")
class DataController(private val localDataService: LocalDataService, private val henrikService: HenrikService) {
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
        return henrikService.getPlayerFromName(username, tag)
    }

    @GetMapping("/matches/light/{username}/{tag}")
    fun getPlayerMatchesLight(@PathVariable username: String, @PathVariable tag: String): Any {
        logger.info("Endpoint fetched: matches light")
        val uuidQuery: Mono<Any> = henrikService.getPlayerFromName(username, tag)
        return uuidQuery.flatMap { result ->
            when (result) {
                is Henrik3Player -> Mono.just(henrikService.getMatchesLightFromUUID(result.data.puuid))
                is HenrikErrors -> Mono.just(result)
                else -> Mono.error(IllegalArgumentException("Unexpected type"))
            }
        }
    }

    @GetMapping("/matches/{name}/{tag}")
    fun getPlayerMatches(@PathVariable name: String, @PathVariable tag: String): Mono<Any> {
        logger.info("Endpoint fetched: matches")
        return henrikService.getStoredMatches(name, tag)
    }

    @GetMapping("/match")
    fun getMatch(@RequestParam("id") matchId: String): Mono<CaupanharmResponse> {
        logger.info("Endpoint fetched: match")


        val isInDb = false
        /*
        Regarder si le match est déjà dans la db, l'y ajouter le cas échéant
         */
        if(isInDb){
            val dbData = null
            return Mono.just(CaupanharmResponse(false, null, dbData)) // TODO récupérer données db
        }else{
            return henrikService.getMatchFromIdV4(matchId).map { response ->
                if (response is HenrikMatchResponseV4) {
                    repository.save(Utils.caupanharmToPostgresMatch(Utils.henrikToCaupanharmMatch(response)))
                    CaupanharmResponse(resolved = true, errorCode = null, body = Utils.henrikToCaupanharmMatch(response))
                } else {
                    CaupanharmResponse(resolved = false, errorCode = null, body = response) // TODO adapter codes et réponse
                }
            }
        }

    }

}