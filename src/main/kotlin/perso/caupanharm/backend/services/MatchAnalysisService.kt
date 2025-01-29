package perso.caupanharm.backend.services

import mu.KotlinLogging
import org.springframework.stereotype.Component
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponse
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponseType
import perso.caupanharm.backend.models.caupanharm.valorant.match.CaupanharmMatchFull
import perso.caupanharm.backend.models.caupanharm.valorant.match.CaupanharmMatchRound
import perso.caupanharm.backend.models.caupanharm.valorant.match.FullMatchAnalysis
import perso.caupanharm.backend.utils.VALORANT_STRINGS
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}


@Component
class MatchAnalysisService {
    fun analyseMatch(playerId: String, match: CaupanharmMatchFull): CaupanharmResponse {
        if(match.metadata.queue != "competitive") return CaupanharmResponse(500, null, CaupanharmResponseType.EXCEPTION, "This endpoint only analyses competitive games")
        val playerTeam = VALORANT_STRINGS.getFromValue(match.players.find { it.playerId == playerId }?.team)
        if(playerTeam == null) return CaupanharmResponse(500, null, CaupanharmResponseType.EXCEPTION, "Player not found")

        // Blue team always start defending
        val attackRounds: MutableList<CaupanharmMatchRound> = mutableListOf()
        val defenseRounds: MutableList<CaupanharmMatchRound> = mutableListOf()
        for(round in match.rounds){
            if(round.roundId <= 11 || (round.roundId >= 24 && round.roundId % 2 == 0)){ // Blue team defending
                if(playerTeam == VALORANT_STRINGS.TEAM_BLUE){
                    defenseRounds.add(round)
                }else{
                    attackRounds.add(round)
                }
            }else{ // Red Team defending
                if(playerTeam == VALORANT_STRINGS.TEAM_BLUE){
                    attackRounds.add(round)
                }else{
                    defenseRounds.add(round)
                }
            }
        }


        /*
        Process :
        1. Split rounds into two lists, attack and defense
        2. Run the same computations for both lists, without averaging anything
        3. Output analysis data, both per side and globally
         */

        return CaupanharmResponse(200, null, CaupanharmResponseType.MATCH_ANALYSIS, FullMatchAnalysis(attackRounds, defenseRounds)) // TODO (placeholder return)

    }
}