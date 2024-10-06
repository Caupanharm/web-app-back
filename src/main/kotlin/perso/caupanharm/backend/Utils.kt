package perso.caupanharm.backend

import perso.caupanharm.backend.models.caupanharm.*
import perso.caupanharm.backend.models.valorant.match.full.CaupanharmMatchFull
import perso.caupanharm.backend.models.valorant.match.full.CaupanharmMatchMetadata
import kotlin.math.round


public class Utils {

    companion object {
        // TODO move this function to the corresponding data classe
        fun caupanharmToPostgresMatch(match: CaupanharmMatchFull): PostGresMatch {
            return PostGresMatch(
                match.metadata.id,
                match.metadata.map,
                match.metadata.gameLengthMillis,
                match.metadata.gameStart,
                match.metadata.queue,
                match.metadata.season,
                match.players,
                match.teams,
                match.rounds,
                match.kills
            )
        }

        // TODO move this function to the corresponding data classe
        fun postgresToCaupanharmMatch(match: PostGresMatch): CaupanharmMatchFull {
            return CaupanharmMatchFull(
                CaupanharmMatchMetadata(
                    match.matchId,
                    match.map,
                    match.gameLengthMillis,
                    match.gameStart,
                    true,
                    match.queue,
                    match.season
                ),
                match.players,
                match.teams,
                match.rounds,
                match.kills
            )
        }

        fun computeGameIssue(team: String, blueScore: Int?, redScore: Int?): String {
            val allyScore = if (team == "Blue") blueScore ?: 0 else redScore ?: 0
            val enemyScore = if (team == "Blue") redScore ?: 0 else blueScore ?: 0

            return if(allyScore + enemyScore == 0) ""
            else if(allyScore > enemyScore) "win"
            else if(allyScore < enemyScore) "lose"
            else "draw"
        }

        fun computeKDA(kills: Int, deaths: Int, assists: Int): String {
            return "$kills/$deaths/$assists"
        }

        fun computeKD(kills: Int, deaths: Int): Double {
            val kd = kills.toDouble() / deaths.toDouble()
            return round(kd * 10) / 10
        }

        // Damage Delta
        fun computeDD(damageDealt: Int, damageReceived: Int, allyScore: Int, enemyScore: Int): Int {
            val dd = (damageDealt - damageReceived).toDouble() / (allyScore + enemyScore)
            return round(dd).toInt()
        }

        fun computeDD(damageDealt: Int, damageReceived: Int, rounds: Int): Int {
            val dd = (damageDealt - damageReceived).toDouble() / rounds
            return round(dd).toInt()
        }

        // Headshot Percentage
        fun computeHSP(headshots: Int, bodyshots: Int, legshots: Int): Int {
            val hsp = headshots.toDouble() / (headshots + bodyshots + legshots) * 100
            return round(hsp).toInt()
        }

        // Bodyshot Percentage
        fun computeBSP(headshots: Int, bodyshots: Int, legshots: Int): Int {
            val hsp = bodyshots.toDouble() / (headshots + bodyshots + legshots) * 100
            return round(hsp).toInt()
        }

        // Legshot Percentage
        fun computeLSP(headshots: Int, bodyshots: Int, legshots: Int): Int {
            val hsp = legshots.toDouble() / (headshots + bodyshots + legshots) * 100
            return round(hsp).toInt()
        }

        // Average damage per round
        fun computeADR(damageDealt: Int, allyScore: Int, enemyScore: Int): Int {
            val adr = damageDealt.toDouble() / (allyScore + enemyScore)
            return round(adr).toInt()
        }

        fun computeADR(damageDealt: Int, rounds: Int): Int {
            val adr = damageDealt.toDouble() / rounds
            return round(adr).toInt()
        }

        // Average combat score
        fun computeACS(combatScore: Int, allyScore: Int, enemyScore: Int): Int {
            val acs = combatScore.toDouble() / (allyScore + enemyScore)
            return round(acs).toInt()
        }

        fun computeACS(combatScore: Int, rounds: Int): Int {
            val acs = combatScore.toDouble() / rounds
            return round(acs).toInt()
        }

    }
}
