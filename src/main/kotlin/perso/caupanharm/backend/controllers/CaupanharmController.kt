package perso.caupanharm.backend.controllers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import perso.caupanharm.backend.repositories.MatchRepository
import perso.caupanharm.backend.services.HenrikService
import perso.caupanharm.backend.services.LocalDataService
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponse
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponseType
import perso.caupanharm.backend.models.caupanharm.valorant.account.CaupanharmPlayer
import perso.caupanharm.backend.models.caupanharm.valorant.database.PostgresMatchAgent
import perso.caupanharm.backend.models.caupanharm.valorant.database.PostgresMatchAgents
import perso.caupanharm.backend.models.localdata.AdditionalCustomPlayerData
import perso.caupanharm.backend.models.localdata.BracketMatchData
import perso.caupanharm.backend.models.localdata.PlayersMatchData
import perso.caupanharm.backend.models.caupanharm.valorant.match.full.CaupanharmMatchFull
import perso.caupanharm.backend.models.caupanharm.valorant.match.full.CaupanharmMatchPlayer
import perso.caupanharm.backend.models.caupanharm.valorant.match.full.CaupanharmMatchScore
import perso.caupanharm.backend.models.caupanharm.valorant.match.full.RiotMatchFull
import perso.caupanharm.backend.models.caupanharm.valorant.matches.CaupanharmMatchHistoryFull
import perso.caupanharm.backend.models.caupanharm.valorant.raw.RawMatchHistory
import perso.caupanharm.backend.transformers.FullMatchTransformer
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.math.abs

val objectMapper = jacksonObjectMapper()


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

    @GetMapping("/matches")
    fun getRawHistory(@RequestParam username: String, region: String = "eu", queue: String = "competitive", start: Int? = 0, end: Int? = 20): Mono<CaupanharmResponse> {
        logger.info("Endpoint fetched: rawHistory with params: username=$username, region=$region, queue=$queue")
        val splittedName = username.split('#')

        return henrikService.getPlayerFromName(splittedName[0], splittedName[1])
            .flatMap { playerResponse ->
                if (playerResponse.statusCode == 200) {
                    val puuid = (playerResponse.body as CaupanharmPlayer).puuid
                    henrikService.getRawHistory(puuid, region, queue, start, end)
                        .flatMap { rawHistoryResponse ->
                            val caupanharmMatches: MutableList<CaupanharmMatchFull> =
                                repository.findByPlayerName(username)
                                    .map { it.toCaupanharmMatchFull() }
                                    .toMutableList()

                            if (rawHistoryResponse.bodyType == CaupanharmResponseType.RAW_MATCH_HISTORY) {
                                val caupanharmMatchesIds: List<String> = caupanharmMatches.map { it.metadata.id }
                                val allMatchesIds = (rawHistoryResponse.body as RawMatchHistory).history.map { it.id }
                                val missingMatchesIds = allMatchesIds.subtract(caupanharmMatchesIds.toSet())
                                var savedMatches = caupanharmMatchesIds.size
                                var matchesAdded = 0
                                logger.info("Matches found: ${rawHistoryResponse.body.total}")
                                logger.info("Matches initially stored: ${caupanharmMatchesIds.size}")

                                // Flux pour récupérer tous les matchs manquants
                                return@flatMap Flux.fromIterable(missingMatchesIds)
                                    .index()
                                    .flatMap { tuple ->
                                        val index = tuple.t1
                                        val matchId = tuple.t2
                                        henrikService.getMatchFromIdV4(
                                            matchId,
                                            missingMatchesIds.size,
                                            index.toInt() + 1
                                        )
                                            .filter { it.bodyType == CaupanharmResponseType.MATCH_FULL }
                                            .map { it.body as CaupanharmMatchFull }
                                            .doOnNext { match ->
                                                try {
                                                    repository.save(match.toPostgresMatch())
                                                    matchesAdded++
                                                    savedMatches++
                                                    caupanharmMatches.add(match)
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

    // TODO complete and replace /match with
    @GetMapping("/match")
    fun getRawMatch(@RequestParam("id") matchId: String, @RequestParam("queue") queue: String = "eu"): Mono<CaupanharmResponse>{
        logger.info("Endpoint fetched: match with params: matchId=${matchId}")

        return henrikService.getRawMatch(matchId, queue)
            .map{ response ->
                if(response.statusCode == 200){
                    CaupanharmResponse(200, null, CaupanharmResponseType.MATCH_FULL, (response.body as RiotMatchFull).toCaupanharmMatchFull())
                }else{
                    response
                }
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

    @GetMapping("teams")
    fun getTeams(
        @RequestParam("username") player: String,
        @RequestParam("agents") agents: String
    ): Mono<CaupanharmResponse> {
        logger.info("Endpoint fetched: teams with params: username=$player, agents=$agents")
        try {
            val rawResults = repository.findTeamsByPlayerName(player)
            if (rawResults.isEmpty()) return Mono.just(
                CaupanharmResponse(
                    500,
                    "No matches found for player $player",
                    CaupanharmResponseType.EXCEPTION,
                    null
                )
            )

            val matches = rawResults.map { result ->
                val players: List<CaupanharmMatchPlayer> = objectMapper.readValue(result["players"] as String)
                val score: CaupanharmMatchScore = objectMapper.readValue(result["score"] as String)

                val foundPlayer = players.find { it.name.equals(player, ignoreCase = true) }
                val playerTeam = foundPlayer!!.team // null safe, checked at rawResults.isEmpty()
                PostgresMatchAgents(
                    playerTeam = playerTeam,
                    agents = players.map { PostgresMatchAgent(it.agent, it.team) },
                    score = score
                )
            }

            var totalPlayed = 0
            var totalWins = 0
            var totalLosses = 0
            var globalScoreDifference = 0
            var globalScoreDifferenceWhenWinning = 0
            var globalScoreDifferenceWhenLosing = 0

            var playedWith = 0
            var wonWith = 0
            var lostWith = 0
            var scoreDifferenceWhenWinningWith = 0
            var scoreDifferenceWhenLosingWith = 0

            var playedAgainst = 0
            var lostAgainst = 0
            var wonAgainst = 0
            var scoreDifferenceWhenLosingAgainst = 0
            var scoreDifferenceWhenWinningAgainst = 0

            val agentsSearch: List<String> = agents.split(',')

            matches.forEach { match ->
                // Grouper les agents par équipe
                val agentsByTeam = match.agents.groupBy { it.team }

                // Trouver toutes les équipes qui contiennent tous les agents recherchés (permet de prendre en compte le cas où les agents sont dans les deux équipes)
                val matchingTeams = agentsByTeam.filter { (_, agents) ->
                    agentsSearch.all { agent -> agent in agents.map { it.agent } }
                }

                // Pour chaque équipe trouvée
                matchingTeams.forEach { (foundTeam, _) ->
                    totalPlayed++
                    if (foundTeam == match.playerTeam) playedWith++ else playedAgainst++

                    // Déterminer l'équipe gagnante
                    val winningTeam = when {
                        match.score.blue > match.score.red -> "Blue"
                        match.score.red > match.score.blue -> "Red"
                        else -> null // Égalité
                    }

                    val matchRoundsDifference = abs(match.score.blue - match.score.red)
                    if (foundTeam == winningTeam) {
                        totalWins++
                        globalScoreDifference += matchRoundsDifference
                        globalScoreDifferenceWhenWinning += matchRoundsDifference

                        if (foundTeam == match.playerTeam) {
                            wonWith++
                            scoreDifferenceWhenWinningWith += matchRoundsDifference
                        } else {
                            lostAgainst++
                            scoreDifferenceWhenLosingAgainst += matchRoundsDifference
                        }


                    } else if (winningTeam != null) { // Si l'équipe a perdu (pas d'égalité)
                        totalLosses++
                        globalScoreDifference -= matchRoundsDifference
                        globalScoreDifferenceWhenLosing += matchRoundsDifference

                        if (foundTeam == match.playerTeam) {
                            lostWith++
                            scoreDifferenceWhenLosingWith += matchRoundsDifference
                        } else {
                            wonAgainst++
                            scoreDifferenceWhenWinningAgainst += matchRoundsDifference

                        }


                    }
                }
            }

            var winrate = if (totalPlayed == 0) null else totalWins.toDouble() / totalPlayed * 100
            var averageScoreDifference = if (totalPlayed == 0) null else globalScoreDifference.toDouble() / totalPlayed
            var averageScoreDifferenceWhenWinning = if (totalWins == 0) null else globalScoreDifferenceWhenWinning.toDouble() / totalWins
            var averageScoreDifferenceWhenLosing = if (totalLosses == 0) null else globalScoreDifferenceWhenLosing.toDouble() / totalLosses

            var winrateWith = if (playedWith == 0) null else wonWith.toDouble() / playedWith * 100
            var averageScoreDifferenceWith = if (playedWith == 0) null else (scoreDifferenceWhenWinningWith - scoreDifferenceWhenLosingWith).toDouble() / playedWith
            var averageScoreDifferenceWhenWinningWith = if (playedWith == 0) null else scoreDifferenceWhenWinningWith.toDouble() / wonWith
            var averageScoreDifferenceWhenLosingWith = if (playedWith == 0) null else scoreDifferenceWhenLosingWith.toDouble() / lostWith

            var winrateAgainst = if (playedAgainst == 0) null else wonAgainst.toDouble() / playedAgainst * 100
            var averageScoreDifferenceAgainst = if (playedAgainst == 0) null else (scoreDifferenceWhenWinningAgainst - scoreDifferenceWhenLosingAgainst).toDouble() / playedAgainst
            var averageScoreDifferenceWhenWinningAgainst = if (playedAgainst == 0) null else scoreDifferenceWhenWinningAgainst.toDouble() / wonAgainst
            var averageScoreDifferenceWhenLosingAgainst = if (playedAgainst == 0) null else scoreDifferenceWhenLosingAgainst.toDouble() / lostAgainst

            println(
                    "Moyennes de la compo $agentsSearch dans les parties de $player:\n\n" +
                            "Au global:\n" +
                            "Winrate: $winrate% ($totalWins/$totalPlayed)\n" +
                            "Ecart au score: $averageScoreDifference\n" +
                            "Ecart au score en cas de victoire: $averageScoreDifferenceWhenWinning\n" +
                            "Ecart au score en cas de défaite: $averageScoreDifferenceWhenLosing\n\n" +
                            "Avec cette compo:\n" +
                            "Winrate: $winrateWith% ($wonWith/$playedWith)\n" +
                            "Ecart au score: $averageScoreDifferenceWith\n" +
                            "Ecart au score en cas de victoire: $averageScoreDifferenceWhenWinningWith\n" +
                            "Ecart au score en cas de défaite: $averageScoreDifferenceWhenLosingWith\n\n" +
                            "Contre cette compo:\n" +
                            "Winrate: $winrateAgainst% ($wonAgainst/$playedAgainst)\n" +
                            "Ecart au score: $averageScoreDifferenceAgainst\n" +
                            "Ecart au score en cas de victoire: $averageScoreDifferenceWhenWinningAgainst\n" +
                            "Ecart au score en cas de défaite: $averageScoreDifferenceWhenLosingAgainst\n\n"
                )

            return Mono.just(CaupanharmResponse(200, null, CaupanharmResponseType.MATCHES_AGENTS_ANALYSIS, matches))
        } catch (e: Exception) {
            logger.error(e.stackTraceToString())
            return Mono.just(CaupanharmResponse(500, null, CaupanharmResponseType.EXCEPTION, e.toString()))
        }
    }



}