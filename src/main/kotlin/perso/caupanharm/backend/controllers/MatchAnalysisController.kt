package perso.caupanharm.backend.controllers;

import mu.KotlinLogging
import kotlin.text.Regex;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponse
import perso.caupanharm.backend.models.riot.RiotMatchFull
import perso.caupanharm.backend.services.HenrikService;
import perso.caupanharm.backend.services.MatchAnalysisService
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/analysis")
class MatchAnalysisController(
    private val henrikService:HenrikService,
    private val matchAnalysisService: MatchAnalysisService
){

    @GetMapping("/match")
    fun analyseMatch(@RequestParam("matchId") matchId: String, @RequestParam("playerId") playerId: String, @RequestParam("region") region: String): Mono<CaupanharmResponse> {
        logger.info("Endpoint fetched: /analysis/match with params: matchId=$matchId, playerId=$playerId, region=$region")
        return henrikService.getMatch(matchId, region)
            .map{ response ->
                if(response.statusCode != 200){
                    response
                }else{
                    matchAnalysisService.analyseMatch(playerId, (response.body as RiotMatchFull).toCaupanharmMatchFull())
                }
            }
    }

}
