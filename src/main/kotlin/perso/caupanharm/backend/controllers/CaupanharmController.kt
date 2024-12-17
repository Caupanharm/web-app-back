package perso.caupanharm.backend.controllers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.*
import perso.caupanharm.backend.repositories.FullMatchRepository
import perso.caupanharm.backend.services.HenrikService
import perso.caupanharm.backend.services.LocalDataService
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponse
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponseType
import perso.caupanharm.backend.models.caupanharm.valorant.account.CaupanharmPlayer
import perso.caupanharm.backend.models.caupanharm.valorant.analysis.*
import perso.caupanharm.backend.models.caupanharm.valorant.database.*
import perso.caupanharm.backend.models.localdata.AdditionalCustomPlayerData
import perso.caupanharm.backend.models.localdata.BracketMatchData
import perso.caupanharm.backend.models.localdata.PlayersMatchData
import perso.caupanharm.backend.models.caupanharm.valorant.match.CaupanharmMatchFull
import perso.caupanharm.backend.models.riot.RiotMatchFull
import perso.caupanharm.backend.models.caupanharm.valorant.matches.CaupanharmMatchHistoryFull
import perso.caupanharm.backend.models.riot.RawMatch
import perso.caupanharm.backend.models.riot.RawMatchHistory
import perso.caupanharm.backend.models.riot.assets.Agents
import perso.caupanharm.backend.repositories.MapStatsRepository
import perso.caupanharm.backend.repositories.MatchXSAgentRepository
import perso.caupanharm.backend.repositories.MatchXSRepository
import perso.caupanharm.backend.transformers.FullMatchTransformer
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import kotlin.collections.HashSet

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
    lateinit var fullMatchRepository: FullMatchRepository

    @Autowired
    lateinit var matchXSRepository: MatchXSRepository

    @Autowired
    lateinit var matchXSAgentRepository: MatchXSAgentRepository

    @Autowired
    lateinit var mapStatsRepository: MapStatsRepository

    @Value("\${valorant.current.maps}")
    lateinit var mapPool: List<String>

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

    @GetMapping("/playerStats")
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
    fun getRawHistory(
        @RequestParam username: String,
        region: String = "eu",
        queue: String = "competitive",
        start: Int? = 0,
        end: Int? = 20
    ): Mono<CaupanharmResponse> {
        logger.info("Endpoint fetched: rawHistory with params: username=$username, region=$region, queue=$queue")
        return henrikService.getPlayerFromName(username)
            .flatMap { playerResponse ->
                if (playerResponse.statusCode == 200) {
                    val puuid = (playerResponse.body as CaupanharmPlayer).puuid
                    henrikService.getHistory(puuid, region, queue, start, end)
                        .flatMap { rawHistoryResponse ->
                            val caupanharmMatches: MutableList<CaupanharmMatchFull> =
                                fullMatchRepository.findByPlayerName(username)
                                    .map { it.toCaupanharmMatchFull() }
                                    .toMutableList()

                            if (rawHistoryResponse.bodyType == CaupanharmResponseType.RAW_MATCH_HISTORY) {
                                val caupanharmMatchesIds: List<String> = caupanharmMatches.map { it.metadata.matchId }
                                val allMatchesIds =
                                    (rawHistoryResponse.body as RawMatchHistory).history.map { it.matchId }
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
                                                    fullMatchRepository.save(caupanharmMatchFull.toPostgresMatch())
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
    fun getRawMatch(@RequestParam("id") matchId: String, @RequestParam("queue") region: String = "eu"): Mono<String> {
        logger.info("Endpoint fetched: rawMatch with params: matchId=${matchId}")
        return henrikService.getRawMatch(matchId, region)
    }

    @GetMapping("/match")
    fun getMatch(
        @RequestParam("id") matchId: String,
        @RequestParam("queue") region: String = "eu"
    ): Mono<CaupanharmResponse> {
        logger.info("Endpoint fetched: match with params: matchId=${matchId}")

        return henrikService.getMatch(matchId, region)
            .map { response ->
                if (response.statusCode == 200) {
                    val match = (response.body as RiotMatchFull).toCaupanharmMatchFull()
                    if (fullMatchRepository.countByMatchId(matchId) == 0) fullMatchRepository.save(match.toPostgresMatch())
                    CaupanharmResponse(200, null, CaupanharmResponseType.MATCH_FULL, match)
                } else {
                    response
                }
            }
    }

    @GetMapping("/matchXS")
    fun getMatchXS(
        @RequestParam("id") matchId: String,
        @RequestParam("queue") region: String = "eu"
    ): Mono<CaupanharmResponse> {
        logger.info("Endpoint fetched: matchXS with params: matchId=${matchId}")


        return henrikService.getMatch(matchId, region)
            .map { response ->
                if (response.statusCode == 200) {
                    val match = (response.body as RiotMatchFull).toCaupanharmMatchFull()
                    matchXSRepository.save(match.toPostgresMatchXS())
                    matchXSAgentRepository.saveAll(match.toPostgresMatchXSAgents())
                    CaupanharmResponse(200, null, CaupanharmResponseType.MATCH_XS, match.toPostgresMatchXSAgents())
                } else {
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
        val match = fullMatchRepository.findByMatchId(matchId)
        return if (match != null) {
            fullMatchTransformer.analyseFullMatch(player, match.toCaupanharmMatchFull())
        } else {
            Mono.just(CaupanharmResponse(500, "Match not found", CaupanharmResponseType.EXCEPTION, matchId))
        }

    }

    @GetMapping("stats")
    fun getMapsAgentsStats(): Mono<CaupanharmResponse> {
        logger.info("Endpoint fetched: stats")
        val data = mapStatsRepository.getData()
        val formattedData = mutableListOf<MapStats>()

        // Global data (no specific agent)
        val allMapsStats = data.first { it.map == null }
        formattedData.add(MapStats(null, allMapsStats.gamesPlayed, playRate = null, allMapsStats.atkWinRate, allMapsStats.defWinRate, mutableListOf()))

        // Single map data (no specific agent)
        mapPool.forEach { map ->
            val mapStats = data.first { it.map == map && it.agent == null }
            formattedData.add(MapStats(map, mapStats.gamesPlayed, mapStats.playRate, mapStats.atkWinRate, mapStats.defWinRate, mutableListOf()))
        }

        // Global data (by agent)
        data.filter{row -> row.map == null && row.agent != null}.forEach { row ->
            formattedData.first{ it.name == null }.topAgents.add(
                MapStatsAgents(row.agent!!, row.gamesPlayed, row.playRate, row.pickRate!!, row.winRate!!, row.atkWinRate, row.defWinRate)
            )
        }

        // Single map data (by agent)
        data.filter{row -> row.map != null && row.agent != null}.forEach { row ->
            formattedData.first{ it.name == row.map }.topAgents.add(
                MapStatsAgents(row.agent!!, row.gamesPlayed, row.playRate, row.pickRate!!, row.winRate!!, row.atkWinRate, row.defWinRate)
            )
        }

        formattedData.forEach{ map -> map.topAgents.sortByDescending { it.winrate } }

        return Mono.just(CaupanharmResponse(200,null,CaupanharmResponseType.MAPS_STATS,formattedData))
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Paris") // Adapt for testing in dev env if needed
    fun saveMapsAgentsStats() {
        logger.info("Called saveMapsStats")
        val computedStats = mutableListOf<PostGresMapAgentsStats>()

        val allMaps = matchXSRepository.getMapRates(mapPool)
        val allMapsStats = PostGresMapAgentsStats(
            map = null,
            agent = null,
            gamesPlayed = (allMaps["games_played"] as Long).toInt(),
            playRate = 1.0,
            pickRate = null,
            winRate = null,
            atkWinRate = allMaps["attack_winrate"] as Double,
            defWinRate = allMaps["defense_winrate"] as Double
        )
        computedStats.add(allMapsStats)

        Agents.entries.forEach { requestedAgent ->
            val currentAgentAllMaps = matchXSAgentRepository.getMapAgentWinrate(null, requestedAgent.displayName)
            computedStats.add(
                PostGresMapAgentsStats(
                    map = null,
                    agent = requestedAgent.displayName,
                    gamesPlayed = (currentAgentAllMaps["games_played"] as Long).toInt(),
                    playRate = currentAgentAllMaps["presence_rate"] as Double,
                    pickRate = currentAgentAllMaps["pick_rate"] as Double,
                    winRate = currentAgentAllMaps["win_rate"] as Double,
                    atkWinRate = currentAgentAllMaps["attack_win_rate"] as Double,
                    defWinRate = currentAgentAllMaps["defense_win_rate"] as Double
                )
            )
        }

        mapPool.forEach { requestedMap ->
            val currentMap = matchXSRepository.getMapRates(listOf(requestedMap))
            val currentMapStats = PostGresMapAgentsStats(
                map = requestedMap,
                agent = null,
                gamesPlayed = (currentMap["games_played"] as Long).toInt(),
                playRate = (currentMap["games_played"] as Long).toDouble() / allMapsStats.gamesPlayed,
                pickRate = null,
                winRate = null,
                atkWinRate = currentMap["attack_winrate"] as Double,
                defWinRate = currentMap["defense_winrate"] as Double
            )
            computedStats.add(currentMapStats)

            Agents.entries.forEach { requestedAgent ->
                val currentAgent = matchXSAgentRepository.getMapAgentWinrate(requestedMap, requestedAgent.displayName)
                computedStats.add(
                    PostGresMapAgentsStats(
                        map = requestedMap,
                        agent = requestedAgent.displayName,
                        gamesPlayed = (currentAgent["games_played"] as Long).toInt(),
                        playRate = currentAgent["presence_rate"] as Double,
                        pickRate = currentAgent["pick_rate"] as Double,
                        winRate = currentAgent["win_rate"] as Double,
                        atkWinRate = currentAgent["attack_win_rate"] as Double,
                        defWinRate = currentAgent["defense_win_rate"] as Double
                    )
                )
            }
        }

        mapStatsRepository.deleteAll()
        mapStatsRepository.saveAll(computedStats)
        logger.info("Maps stats updated")
    }

    @GetMapping("comps")
    fun getCompsCustom(
        @RequestParam("map") map: String?,
        @RequestParam("agents") agentsParam: String?,
        @RequestParam("sort") sortType: String = "bayesian",
        @RequestParam("minCount") minCountParam: Int?
    ): Mono<CaupanharmResponse> {
        logger.info("Endpoint fetched: comps with params: map=$map, agents=$agentsParam, sortType=$sortType, minCount=$minCountParam")
        val requestedAgents = agentsParam?.split(',') ?: emptyList()
        val totalMatchesSaved = matchXSRepository.getNumberOfMatches()
        val matches = matchXSAgentRepository.findMatchesWithAgentsAndMap(map, requestedAgents)
            .map {
                PostGresCompQuery(
                    map = it["map"] as String,
                    allyScore = it["ally_score"] as Int,
                    enemyScore = it["enemy_score"] as Int,
                    attackScore = it["attack_score"] as Int,
                    defenseScore = it["defense_score"] as Int,
                    teamAgents = (it["team_agents"] as String).split(',')
                )
            }

        val comps = mutableMapOf<List<String>, CompStatsCount>()

        matches.forEach { match ->
            val compStats = comps.getOrPut(match.teamAgents) {
                CompStatsCount(0, 0)
            }
            compStats.count++
            if (match.allyScore > match.enemyScore) compStats.wins++
        }

        // Bayesian average
        val globalWinrate = comps.values.sumOf { it.wins }.toDouble() / comps.values.sumOf { it.count }
        val confidence = minCountParam
            ?: comps.values.sortedBy { it.count }[(comps.values.size * 75.0 / 100).toInt()].count // 3rd quartile

        val minCount = minCountParam ?: 0
        var sortedComps = comps.filter { it.value.count > minCount }.map { comp ->
            CompStats(
                comp = comp.key,
                bayesianAverage = (comp.value.wins.toDouble() + confidence * globalWinrate) / (comp.value.count + confidence), // Simplified formula
                timesPlayed = comp.value.count,
                pickRateInAllGames = comp.value.count / (totalMatchesSaved * 2.0),
                pickRateInMatchingComps = comp.value.count.toDouble() / comps.values.sumOf { it.count },
                winRate = comp.value.wins.toDouble() / comp.value.count
            )
        }


        sortedComps = when (sortType) {
            "count" -> sortedComps.sortedWith(compareByDescending<CompStats> { it.timesPlayed }.thenByDescending { it.bayesianAverage })
            "winrate" -> sortedComps.sortedWith(compareByDescending<CompStats> { it.winRate }.thenByDescending { it.bayesianAverage })
            "pickrate" -> sortedComps.sortedWith(compareByDescending<CompStats> { it.pickRateInAllGames }.thenByDescending { it.bayesianAverage })
            "bayesian" -> sortedComps.sortedWith(compareByDescending<CompStats> { it.bayesianAverage }.thenByDescending { it.timesPlayed })
            else -> sortedComps
        }

        val compStatsResponse = CompStatsResponse(
            settings = CompStatsSettings(map, requestedAgents, sortType, confidence, minCount),
            requestedAgentsStats = RequestedAgentsStats(
                timesPlayed = matches.size,
                differentCompsFound = sortedComps.size,
                pickRateInTeam = matches.size / (totalMatchesSaved * 2.0),
                winRate = globalWinrate
            ),
            matchingComps = sortedComps.take(100)
        )

        return Mono.just(CaupanharmResponse(200, null, CaupanharmResponseType.COMP_STATS, compStatsResponse))
    }

    // Using synchronous calls here as this endpoint should later be integrated to another server and not used as an endpoint in Caupanharm
    @GetMapping("populateDatabase")
    fun populateDatabase(@RequestParam("seed") seed: String): CaupanharmResponse {
        val playerResponse = henrikService.getPlayerFromName(seed).block()!!
        Thread.sleep(2200)
        if (playerResponse.statusCode != 200) return playerResponse
        val player = playerResponse.body as CaupanharmPlayer
        var visitedPlayers: MutableSet<String> = HashSet()
        var playersToVisit: MutableSet<String> = HashSet()
        playersToVisit.add(player.puuid)

        logger.info("Starting populating database with seed $seed")
        return populateDatabaseRecursive(player.puuid, player.region, "competitive", visitedPlayers, playersToVisit)
    }

    // 2s delay after each Henrik request to avoid reaching rate limit
    fun populateDatabaseRecursive(
        currentPlayer: String,
        region: String,
        queue: String,
        visitedPlayers: MutableSet<String>,
        playersToVisit: MutableSet<String>
    ): CaupanharmResponse {
        try {
            while (playersToVisit.size > 0) {
                // Find every match
                var firstHistoryResponse = henrikService.getHistory(currentPlayer, region, queue, 0, 20).block()!!
                Thread.sleep(2200)
                while (firstHistoryResponse.statusCode != 200) {
                    logger.info("Got ${firstHistoryResponse.statusCode}   ${firstHistoryResponse.body}")
                    logger.info("Trying again")
                    firstHistoryResponse = henrikService.getHistory(currentPlayer, region, queue, 0, 20).block()!!
                    Thread.sleep(2200)
                }
                val firstResponse = firstHistoryResponse.body as RawMatchHistory
                var foundMatches: MutableList<RawMatch> =
                    firstResponse.history.toMutableList() // Keeping it as a list instead of a set to stop iterating once a given date is reached, since matches are chronogically ordered
                // Stops the loop when there is no more matches (each request retrieves up to 20 matches so if there is only 0 to 19 matches we know we reached the end)
                // or when the last match found is older than 2 months (2*30,44 days = 5259486 seconds)
                while (firstResponse.total - foundMatches.size != 0 && foundMatches[foundMatches.size - 1].startTime / 1000 > (Instant.now().epochSecond - 5259486)) {
                    var nextResponse = henrikService.getHistory(
                        currentPlayer,
                        region,
                        queue,
                        foundMatches.size,
                        20 + foundMatches.size
                    ).block()!!
                    Thread.sleep(2200)
                    while (nextResponse.statusCode != 200) {
                        logger.info("Got ${nextResponse.statusCode}   ${nextResponse.body}")
                        logger.info("Trying again")
                        nextResponse = henrikService.getHistory(
                            currentPlayer,
                            region,
                            queue,
                            foundMatches.size,
                            20 + foundMatches.size
                        ).block()!!
                        Thread.sleep(2200)
                    }
                    val responseBody = nextResponse.body as RawMatchHistory
                    responseBody.history.forEach {
                        if ((it.startTime / 1000) > Instant.now().epochSecond - 5259486) foundMatches.add(
                            it
                        )
                    }

                }
                logger.info("Found ${foundMatches.size} corresponding match IDs")
                // Save every match
                foundMatches.forEach { match ->
                    if (matchXSRepository.countByMatchId(match.matchId) == 0) {
                        var fullMatchResponse = henrikService.getMatch(match.matchId, region).block()!!
                        Thread.sleep(2200)
                        while (fullMatchResponse.statusCode != 200) {
                            logger.info("Got ${fullMatchResponse.statusCode}   ${fullMatchResponse.body}")
                            logger.info("Trying again")
                            fullMatchResponse = henrikService.getMatch(match.matchId, region).block()!!
                            Thread.sleep(2200)
                        }

                        if (fullMatchResponse.bodyType == CaupanharmResponseType.RAW_MATCH) {
                            val fullMatch = (fullMatchResponse.body as RiotMatchFull).toCaupanharmMatchFull()
                            val matchXS = fullMatch.toPostgresMatchXS()
                            val playersXS = fullMatch.toPostgresMatchXSAgents()

                            matchXSRepository.save(matchXS)
                            matchXSAgentRepository.saveAll(playersXS)

                            playersXS.forEach { player ->
                                if (!playersToVisit.contains(player.playerId) && !visitedPlayers.contains(player.playerId)) {
                                    playersToVisit.add(player.playerId)
                                }
                            }
                            logger.info("Saved match ${match.matchId} from player $currentPlayer")
                        } else {
                            logger.info("Couldn't save match ${match.matchId} from player $currentPlayer")
                            logger.info("${fullMatchResponse.statusCode}   ${fullMatchResponse.message}")
                            logger.info(fullMatchResponse.body.toString())
                        }
                    } else { // If the match is already saved, get it from the database just to get its players
                        val savedMatchPlayers: List<String> =
                            matchXSAgentRepository.findPlayerIdByMatchId(match.matchId)
                        savedMatchPlayers.forEach { player ->
                            if (!playersToVisit.contains(player) && !visitedPlayers.contains(player)) {
                                playersToVisit.add(player)
                            }
                        }
                    }
                }

                visitedPlayers.add(currentPlayer)
                playersToVisit.remove(currentPlayer)
                logger.info("Matches saved for player $currentPlayer")
                logger.info("${visitedPlayers.size} players done visited")
                logger.info("${playersToVisit.size} players left to visit")

                val nextPlayer = playersToVisit.random()
                logger.info("Next player to visit: $nextPlayer")
                // Call recursively with a new playerId (chosen randomly to maximize match diversity)
                populateDatabaseRecursive(nextPlayer, region, queue, visitedPlayers, playersToVisit)
            }
            return CaupanharmResponse(200, "Done.", CaupanharmResponseType.EXCEPTION, "Should never happen, unless...")
        } catch (e: Exception) {
            return CaupanharmResponse(500, null, CaupanharmResponseType.EXCEPTION, e.stackTraceToString())
        }
    }


}