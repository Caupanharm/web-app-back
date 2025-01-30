package perso.caupanharm.backend.services

import mu.KotlinLogging
import org.springframework.stereotype.Component
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponse
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponseType
import perso.caupanharm.backend.models.caupanharm.valorant.match.*
import perso.caupanharm.backend.utils.VALORANT_STRINGS

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

        logger.info("Analyzing ${match.rounds.size} rounds")
        val attackImpactAnalysis = computeImpactAnalysis(playerId, attackRounds, match.kills)
        val defenseImpactAnalysis = computeImpactAnalysis(playerId, defenseRounds, match.kills)
        val totalImpactAnalysis = attackImpactAnalysis + defenseImpactAnalysis


        val response = FullMatchAnalysis(
            rounds = match.rounds,
            global = SideAnalysis(totalImpactAnalysis),
            attack = SideAnalysis(attackImpactAnalysis),
            defense = SideAnalysis(defenseImpactAnalysis)
        )

        return CaupanharmResponse(200, null, CaupanharmResponseType.MATCH_ANALYSIS, response)

    }

    fun computeImpactAnalysis(playerId: String, rounds: List<CaupanharmMatchRound>, kills: List<CaupanharmMatchKill>): ImpactAnalysis{
        var totalKills = 0
        var totalDeaths = 0
        var totalScore = 0
        var kastRounds = 0
        var totalClutches = 0
        var wonClutches = 0
        var totalDuels = 0
        var wonDuels = 0

        for(round in rounds){
            val playerStats = round.stats.single{ it.player == playerId }
            val killEvents = kills.filter{ it.round == round.roundId }

            totalKills += killEvents.filter{ it.killer == playerId }.size

            totalDeaths += killEvents.filter{ it.victim == playerId }.size

            totalScore += playerStats.score

            if(hasKAST(playerId, killEvents)) kastRounds++

            if(isClutchSituation()){
                totalClutches++
                if(didClutch()) wonClutches++
            }

            if(isDuelSituation()){
                totalDuels++
                if(didWinDuel()) wonDuels++
            }

        }

        return ImpactAnalysis(
            rounds = rounds.size,
            totalKills = totalKills,
            totalDeaths = totalDeaths,
            kd = if(totalDeaths == 0) totalKills.toDouble() else totalKills.toDouble() / totalDeaths,
            totalScore = totalScore,
            acs = totalScore / rounds.size,
            kastRounds = kastRounds,
            averageKAST = kastRounds.toDouble() / rounds.size,
            totalClutches = totalClutches,
            wonClutches = wonClutches,
            clutchWinRate = if(totalClutches == 0) null else wonClutches.toDouble() / totalClutches,
            totalDuels = totalDuels,
            wonDuels = wonDuels,
            duelWinRate = if(totalDuels == 0) null else wonDuels.toDouble() / totalDuels
        )
    }

    /*
    KAST means getting a (K)ill, an (A)ssist, (S)urviving, or dying but getting (T)raded.
    The definition of a trade is subjective, and we consider here that trading means killing someone's killer within 3 seconds of the initial kill.
     */
    fun hasKAST(playerId: String, killEvents: List<CaupanharmMatchKill>): Boolean{
        if(killEvents.any{ it.killer == playerId }) return true
        if(killEvents.any{ it.assistants.contains(playerId)}) return true

        val deaths = killEvents.filter { it.victim == playerId } // One player can die multiple times in a round (Phoenix ult, Clove ult, Sage revive...)
        if(deaths.isEmpty()) return true

        for(death in deaths){
            val killerId = death.killer
            val playerDeathTime = death.roundTimeMillis
            val killerDeaths = killEvents.filter { it.victim == killerId }
            if(killerDeaths.find{ it.roundTimeMillis - playerDeathTime <= 3000} != null) return true
        }

        return false
    }

    fun isClutchSituation(): Boolean{
        return false //TODO(Not yet implemented)
    }

    fun didClutch(): Boolean{
        return false //TODO(Not yet implemented)
    }

    fun isDuelSituation(): Boolean{
        return false //TODO(Not yet implemented)
    }

    fun didWinDuel(): Boolean{
        return false //TODO(Not yet implemented)
    }
}