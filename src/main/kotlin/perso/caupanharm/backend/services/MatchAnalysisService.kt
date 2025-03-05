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
    private val tradeWindow = 3000 // ms between two kills to be considered a trade
    fun analyseMatch(playerId: String, match: CaupanharmMatchFull): CaupanharmResponse {
        if(match.metadata.queue != "competitive") return CaupanharmResponse(500, null, CaupanharmResponseType.EXCEPTION, "This endpoint only analyses competitive games")
        val player = match.players.find { it.playerId == playerId }
        if(player == null) return CaupanharmResponse(500, null, CaupanharmResponseType.EXCEPTION, "Player not found")
        val playerTeam = VALORANT_STRINGS.getFromValue(player.team)

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

        val attackImpactAnalysis = computeImpactAnalysis(match.players, player, attackRounds, match.kills)
        val defenseImpactAnalysis = computeImpactAnalysis(match.players, player, defenseRounds, match.kills)
        val totalImpactAnalysis = attackImpactAnalysis + defenseImpactAnalysis

        val response = FullMatchAnalysis(
            rounds = match.rounds,
            global = SideAnalysis(totalImpactAnalysis),
            attack = SideAnalysis(attackImpactAnalysis),
            defense = SideAnalysis(defenseImpactAnalysis)
        )

        return CaupanharmResponse(200, null, CaupanharmResponseType.MATCH_ANALYSIS, response)
    }

    fun computeImpactAnalysis(players: List<CaupanharmMatchPlayer>, player: CaupanharmMatchPlayer, rounds: List<CaupanharmMatchRound>, kills: List<CaupanharmMatchKill>): ImpactAnalysis{
        var totalKills = 0
        var totalDeaths = 0
        var totalScore = 0
        var kastRounds = 0
        val clutchSituationsByEnemiesAlive: MutableList<Int> = mutableListOf(0,0,0,0,0,0,0) // max 7 kills including Sage and Clove ultimates
        val wonClutchesByEnemiesAlive: MutableList<Int> = mutableListOf(0,0,0,0,0,0,0)
        var duelSituations = 0
        var wonDuels = 0

        for(round in rounds){
            val playerStats = round.stats.single{ it.player == player.playerId }
            val killEvents = kills.filter{ it.round == round.roundId }

            totalKills += killEvents.filter{ it.killer == player.playerId }.size

            totalDeaths += killEvents.filter{ it.victim == player.playerId }.size

            totalScore += playerStats.score

            if(hasKAST(player, killEvents)) kastRounds++

            // For clutches, ignore rounds where an enemy was AFK
            val afkPlayers = round.stats.filter{it.behavior.afk || it.behavior.inSpawn}
            if(afkPlayers.none { afkPlayer -> players.filter{ it.team != player.team }.any{ it.playerId == afkPlayer.player} }){
                val clutchEnemies = clutchDetection(player, players, VALORANT_STRINGS.getFromValue(round.winningTeam), killEvents)
                if(clutchEnemies in 1..10) clutchSituationsByEnemiesAlive[clutchEnemies - 1]++
                if(clutchEnemies > 10){ // Offset of 10 for a round won
                    clutchSituationsByEnemiesAlive[clutchEnemies - 11]++
                    wonClutchesByEnemiesAlive[clutchEnemies - 11]++
                }
            }

            for(kill in killEvents.filter{ it.killer == player.playerId || it.victim == player.playerId }){
                when(duelDetection()){
                    -1 -> duelSituations++
                    1 -> {
                        duelSituations++
                        wonDuels++
                    }
                }
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
            clutchWinRate = if(clutchSituationsByEnemiesAlive.sum() == 0) null else wonClutchesByEnemiesAlive.sum().toDouble() / clutchSituationsByEnemiesAlive.sum(),
            clutchSituationsByEnemiesAlive = clutchSituationsByEnemiesAlive,
            wonClutchesByEnemiesAlive = wonClutchesByEnemiesAlive,
            duelSituations = duelSituations,
            wonDuels = wonDuels,
            duelWinRate = if(duelSituations == 0) null else wonDuels.toDouble() / duelSituations
        )
    }

    /*
    KAST means getting a (K)ill, an (A)ssist, (S)urviving, or dying but getting (T)raded.
    The definition of a trade is subjective, and we consider here that trading means killing someone's killer within 3 seconds of the initial kill.
     */
    fun hasKAST(player: CaupanharmMatchPlayer, killEvents: List<CaupanharmMatchKill>): Boolean{
        if(killEvents.any{ it.killer == player.playerId }) return true
        if(killEvents.any{ it.assistants.contains(player.playerId)}) return true

        val deaths = killEvents.filter { it.victim == player.playerId } // One player can die multiple times in a round (Phoenix ult, Clove ult, Sage revive...)
        if(deaths.isEmpty()) return true

        for(death in deaths){
            val killerId = death.killer
            val playerDeathTime = death.roundTimeMillis
            val killerDeaths = killEvents.filter { it.victim == killerId }
            if(killerDeaths.find{ it.roundTimeMillis - playerDeathTime <= tradeWindow} != null) return true
        }

        return false
    }

    /**
     * Detects if a player got in a clutch situation for a given round
     * @param player The player to analyze
     * @param players The list of all players
     * @param winningTeam The team that won that round
     * @param kills The list of kills that happened that round
     *
     * The globally admitted definition of a clutch is when a player wins a round while being the last player of their team alive,
     * regardless of the round closing ceremony.
     *
     * Here is how I decided to process, to prevent most edge cases from giving false positives or negatives :
     * - Map every player and mark them as alive on round start
     * - Iterate through the kill feed, marking them as dead as they die, or alive again as they get a kill (meaning they got revived inbetween)
     * - Count how many players are alive after each kill to know when the player is 1 vs N
     * - If this happens multiple times, only the last one will be considered for the clutch situation
     * - A trade does not count towards the clutch. For example in a 2v3 situation, if an ally dies (1v3) but gets traded (1v2), we will count the clutch situation we turn into as a 1v2
     *
     * It is possible that an enemy was revived during the clutch situation, effectively adding 1 enemy to the list of enemies the player is clutching against.
     * To account for this, after a clutch situation is confirmed we recompute the number of enemies through the rest of the kills list.
     *
     * This algorithm ignores a few edge cases, that are rare enough in my opinion to actively influence statistics. These include :
     *  - An ally gets revived after the player gets into a clutch situation, but doesn't get any kill and doesn't die again.
     *    There is no way for us to know from the kills list that the player is no longer alone in their team.
     *  - Same scenario for an enemy: we can't tell that the player is clutching against 1 more enemy
     *
     * @return 0 if no clutch was detected, N if a 1 versus N clutch was detected, N+10 if that clutch was won
     */
    fun clutchDetection(player: CaupanharmMatchPlayer, players: List<CaupanharmMatchPlayer>, winningTeam: VALORANT_STRINGS, kills: List<CaupanharmMatchKill>): Int{
        val alliesMap: MutableMap<String, Boolean> = mutableMapOf()
        val enemiesMap: MutableMap<String, Boolean> = mutableMapOf()
        for(p in players){
            (if (p.team == player.team) alliesMap else enemiesMap)[p.playerId] = true // True if alive, false if dead
        }

        var clutchStartingKillId: Int? = null
        var clutchStartingEnemies: Int? = null
        for(killEvent in kills.dropLast(1)){ // Dismissing the very last kill as it can only be the end of a clutch situation, not the start
            // If a player died to the bomb, we still count them as alive for the sake of the potential clutch situation
            if(killEvent.weapon.damageType != VALORANT_STRINGS.BOMB_KILL.formatted){
                if(players.find{ it.playerId == killEvent.victim}!!.team == player.team){
                    alliesMap[killEvent.victim] = false
                    if(killEvent.killer != null) enemiesMap[killEvent.killer] = true
                }else{
                    enemiesMap[killEvent.victim] = false
                    if(killEvent.killer != null) alliesMap[killEvent.killer] = true
                }
            }

            when(alliesMap.filter { it.value }.size){ // Depending on the number of teammates of player alive after a kill
                0 -> break // None: exit the loop, having found a clutch situation or not
                1 -> {
                    if(alliesMap[player.playerId] == true){ // Player is last alive in their team: potential clutch situation
                        if((clutchStartingEnemies == null
                            || enemiesMap.filter { it.value }.size > clutchStartingEnemies) // Making sure not to overwrite the same clutch, as getting a kill while already being in clutch situation will also trigger this case
                            && (killEvent.roundTimeMillis + tradeWindow < kills[kills.indexOf(killEvent) + 1].roundTimeMillis
                                    || kills[kills.indexOf(killEvent) +1].victim != killEvent.killer)){ // Also asserting the kill is not a simple trade from a 2 vs N situation
                            clutchStartingKillId = kills.indexOf(killEvent)
                            clutchStartingEnemies = enemiesMap.filter { it.value }.size
                        }
                    }
                }
                else -> { // Helps resetting stored clutches in case a teammate is revived
                    clutchStartingKillId = null
                    clutchStartingEnemies = null
                }
            }
        }

        // If a clutch situation was detected, we lastly need to recheck how many enemies were alive as some may have been revived in between
        // If we count more enemies during the clutch than what was counted at its start, then we keep that new value
        // This only leaves a very few edge cases giving incorrect values,
        // such as an enemy getting revived during the clutch and dying to the bomb, or an enemy being revived multiple times
        if(clutchStartingKillId != null){
            var clutchEnemies: MutableSet<String> = mutableSetOf()
            for(i in clutchStartingKillId+1..<kills.size){
                val killEvent = kills[i]
                if(enemiesMap.keys.contains(killEvent.killer)) clutchEnemies.add(killEvent.killer!!) // Asserted by the if condition
                if(enemiesMap.keys.contains(killEvent.victim)) clutchEnemies.add(killEvent.victim)
            }
            if(clutchEnemies.size > clutchStartingEnemies!!) clutchStartingEnemies = clutchEnemies.size // Asserted since is only null if clutchStartingKillId is also null
        }


        return if(clutchStartingKillId == null){ // if not null, then clutchStartingEnemies cannot be null
            0
        }else{
            if(player.team == winningTeam.formatted){
                clutchStartingEnemies!! + 10
            }else{
                clutchStartingEnemies!!
            }
        }
    }

    /*
    Returns 0 if the kill wasn't a duel, -1 if it was a lost duel, 1 if it was won
     */
    fun duelDetection(): Int{
        return 0 //TODO(Not yet implemented)
    }

}