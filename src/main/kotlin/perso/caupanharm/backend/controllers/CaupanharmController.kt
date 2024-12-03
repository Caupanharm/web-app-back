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
import perso.caupanharm.backend.models.caupanharm.valorant.match.CaupanharmMatchFull
import perso.caupanharm.backend.models.caupanharm.valorant.match.CaupanharmMatchPlayer
import perso.caupanharm.backend.models.caupanharm.valorant.match.CaupanharmMatchScore
import perso.caupanharm.backend.models.riot.RiotMatchFull
import perso.caupanharm.backend.models.caupanharm.valorant.matches.CaupanharmMatchHistoryFull
import perso.caupanharm.backend.models.riot.RawMatch
import perso.caupanharm.backend.models.riot.RawMatchHistory
import perso.caupanharm.backend.transformers.FullMatchTransformer
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*
import kotlin.collections.HashSet
import kotlin.math.abs

val objectMapper = jacksonObjectMapper()
private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api")
class CaupanharmController(
    private val localDataService: LocalDataService,
    private val henrikService: HenrikService,
    private val fullMatchTransformer: FullMatchTransformer

) {
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
        return henrikService.getPlayerFromName(username)
    }

    @GetMapping("/matches")
    fun getRawHistory(@RequestParam username: String, region: String = "eu", queue: String = "competitive", start: Int? = 0, end: Int? = 20): Mono<CaupanharmResponse> {
        logger.info("Endpoint fetched: rawHistory with params: username=$username, region=$region, queue=$queue")
        return henrikService.getPlayerFromName(username)
            .flatMap { playerResponse ->
                if (playerResponse.statusCode == 200) {
                    val puuid = (playerResponse.body as CaupanharmPlayer).puuid
                    henrikService.getHistory(puuid, region, queue, start, end)
                        .flatMap { rawHistoryResponse ->
                            val caupanharmMatches: MutableList<CaupanharmMatchFull> =
                                repository.findByPlayerName(username)
                                    .map { it.toCaupanharmMatchFull() }
                                    .toMutableList()

                            if (rawHistoryResponse.bodyType == CaupanharmResponseType.RAW_MATCH_HISTORY) {
                                val caupanharmMatchesIds: List<String> = caupanharmMatches.map { it.metadata.matchId }
                                val allMatchesIds = (rawHistoryResponse.body as RawMatchHistory).history.map { it.matchId }
                                val missingMatchesIds = allMatchesIds.subtract(caupanharmMatchesIds.toSet())
                                var savedMatches = caupanharmMatchesIds.size
                                var matchesAdded = 0
                                logger.info("Matches found: ${rawHistoryResponse.body.total}")
                                logger.info("Matches initially stored: ${caupanharmMatchesIds.size}")

                                // Flux pour récupérer tous les matchs manquants
                                return@flatMap Flux.fromIterable(missingMatchesIds)
                                    .flatMap { matchId ->
                                        henrikService.getMatch(matchId, region)
                                            .filter { it.bodyType == CaupanharmResponseType.RAW_MATCH }
                                            .map { it.body as RiotMatchFull }
                                            .doOnNext { match ->
                                                try {
                                                    val caupanharmMatchFull = match.toCaupanharmMatchFull()
                                                    repository.save(caupanharmMatchFull.toPostgresMatch())
                                                    matchesAdded++
                                                    savedMatches++
                                                    caupanharmMatches.add(caupanharmMatchFull)
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

    @GetMapping("/rawMatch")
    fun getRawMatch(@RequestParam("id") matchId: String, @RequestParam("queue") region: String = "eu"): Mono<String>{
        logger.info("Endpoint fetched: rawMatch with params: matchId=${matchId}")
        return henrikService.getRawMatch(matchId, region)
    }

    @GetMapping("/match")
    fun getMatch(@RequestParam("id") matchId: String, @RequestParam("queue") region: String = "eu"): Mono<CaupanharmResponse>{
        logger.info("Endpoint fetched: match with params: matchId=${matchId}")

        return henrikService.getMatch(matchId, region)
            .map{ response ->
                if(response.statusCode == 200){
                    val match = (response.body as RiotMatchFull).toCaupanharmMatchFull()
                    if(repository.countByMatchId(matchId) == 0) repository.save(match.toPostgresMatch())
                    CaupanharmResponse(200, null, CaupanharmResponseType.MATCH_FULL, match)
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
        val match = repository.findByMatchId(matchId)
        return if(match != null) {
                fullMatchTransformer.analyseFullMatch(player, match.toCaupanharmMatchFull())
            } else {
                Mono.just(CaupanharmResponse(500, "Match not found",CaupanharmResponseType.EXCEPTION, matchId))
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

    // Using synchronous calls here as this endpoint should later be integrated to another server and not used as an endpoint in Caupanharm
    @GetMapping("populateDatabase")
    fun populateDatabase(@RequestParam("seed") seed: String): CaupanharmResponse{
        val playerResponse = henrikService.getPlayerFromName(seed).block()!!
        Thread.sleep(3000)
        if (playerResponse.statusCode != 200) return playerResponse
        val player = playerResponse.body as CaupanharmPlayer
        var visitedPlayers: MutableSet<String> = HashSet()
        var playersToVisit: Queue<String> = LinkedList()
        playersToVisit.add(player.puuid)

        logger.info("Starting populating database with seed $seed")
        return populateDatabaseRecursive(player.puuid, player.region, "competitive", visitedPlayers, playersToVisit)
    }

    // 2s delay after each Henrik request to avoid reaching rate limit
    fun populateDatabaseRecursive(playerId: String, region: String, queue: String, visitedPlayers: MutableSet<String>, playersToVisit: Queue<String>): CaupanharmResponse{
        try{
            while(playersToVisit.size > 0){
                val currentPlayer = playersToVisit.remove()
                logger.info("Checking player $currentPlayer, region $region, queue $queue")
                logger.info("${visitedPlayers.size} players already visited")
                logger.info("${playersToVisit.size} more players to visit")
                // For every player that hasn't been checked already
                if(!visitedPlayers.contains(currentPlayer)){
                    // Find every match
                    var firstHistoryResponse = henrikService.getHistory(currentPlayer, region, queue, 0, 20).block()!!
                    Thread.sleep(3000)
                    while(firstHistoryResponse.statusCode != 200) {
                        logger.info("Got ${firstHistoryResponse.statusCode}   ${firstHistoryResponse.body}")
                        logger.info("Trying again")
                        firstHistoryResponse = henrikService.getHistory(currentPlayer, region, queue, 0, 20).block()!!
                        Thread.sleep(3000)
                    }
                    val firstResponse = firstHistoryResponse.body as RawMatchHistory
                    var foundMatches: MutableList<RawMatch> = firstResponse.history.toMutableList() // Keeping it as a list instead of a set to stop iterating once a given date is reached, since matches are chronogically ordered
                    // Stops the loop when there is no more matches (each request retrieves up to 20 matches so if there is only 0 to 19 matches we know we reached the end)
                    // or when the last match found is older than 2 months (2*30,44 days = 5259486 seconds)
                    while(firstResponse.total - foundMatches.size != 0 && foundMatches[foundMatches.size-1].startTime / 1000 > (Instant.now().epochSecond - 5259486)){
                        var nextResponse = henrikService.getHistory(currentPlayer, region, queue, foundMatches.size, 20+foundMatches.size).block()!!
                        Thread.sleep(3000)
                        while(nextResponse.statusCode != 200){
                            logger.info("Got ${nextResponse.statusCode}   ${nextResponse.body}")
                            logger.info("Trying again")
                            nextResponse = henrikService.getHistory(currentPlayer, region, queue, foundMatches.size, 20+foundMatches.size).block()!!
                            Thread.sleep(3000)
                        }
                        val responseBody = nextResponse.body as RawMatchHistory
                        responseBody.history.forEach { if((it.startTime / 1000) > Instant.now().epochSecond - 5259486) foundMatches.add(it) }

                    }
                    logger.info("Found ${foundMatches.size} corresponding match IDs")

                    // Save every match
                    foundMatches.forEach{ match ->
                        if(repository.countByMatchId(match.matchId) == 0){
                            var fullMatchResponse = henrikService.getMatch(match.matchId, region).block()!!
                            Thread.sleep(3000)
                            while(fullMatchResponse.statusCode != 200){
                                logger.info("Got ${fullMatchResponse.statusCode}   ${fullMatchResponse.body}")
                                logger.info("Trying again")
                                fullMatchResponse = henrikService.getMatch(match.matchId, region).block()!!
                                Thread.sleep(3000)
                            }

                            if(fullMatchResponse.bodyType == CaupanharmResponseType.RAW_MATCH){
                                val fullMatch = fullMatchResponse.body as RiotMatchFull
                                fullMatch.players.forEach { player ->
                                    if(!playersToVisit.contains(player.subject)){
                                        playersToVisit.add(player.subject)
                                    }
                                }
                                repository.save(fullMatch.toCaupanharmMatchFull().toPostgresMatch())
                                logger.info("Saved match ${match.matchId} from player $currentPlayer")
                            }else{
                                logger.info("Couldn't save match ${match.matchId} from player $currentPlayer")
                                logger.info("${fullMatchResponse.statusCode}   ${fullMatchResponse.message}")
                                logger.info(fullMatchResponse.body.toString())
                            }
                        }else{
                            logger.info("Match ${match.matchId} from player $currentPlayer already saved")
                            if(match == foundMatches[0]){
                                var fullMatchResponse = henrikService.getMatch(match.matchId, region).block()!!
                                Thread.sleep(3000)
                                while(fullMatchResponse.statusCode != 200){
                                    logger.info("Got ${fullMatchResponse.statusCode}   ${fullMatchResponse.body}")
                                    logger.info("Trying again")
                                    fullMatchResponse = henrikService.getMatch(match.matchId, region).block()!!
                                    Thread.sleep(3000)
                                }
                                if(fullMatchResponse.bodyType == CaupanharmResponseType.RAW_MATCH){
                                    val fullMatch = fullMatchResponse.body as RiotMatchFull
                                    fullMatch.players.forEach { player ->
                                        if(!playersToVisit.contains(player.subject)){
                                            playersToVisit.add(player.subject)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    visitedPlayers.add(currentPlayer)
                    // Call recursively with a new playerId
                    populateDatabaseRecursive(playersToVisit.peek(), region, queue, visitedPlayers, playersToVisit)
                }
            }
            return CaupanharmResponse(200, "Done.", CaupanharmResponseType.EXCEPTION, "Should never happen, unless...")
        }catch(e: Exception){
            return CaupanharmResponse(500, null, CaupanharmResponseType.EXCEPTION, e.stackTraceToString())
        }
    }
}