package perso.caupanharm.backend.controllers

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponse
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponseType
import perso.caupanharm.backend.models.caupanharm.valorant.account.CaupanharmPlayer
import perso.caupanharm.backend.models.caupanharm.valorant.analysis.*
import perso.caupanharm.backend.models.caupanharm.valorant.database.PostGresAgentsStats
import perso.caupanharm.backend.models.caupanharm.valorant.database.PostGresCompQuery
import perso.caupanharm.backend.models.caupanharm.valorant.database.PostGresCompsStats
import perso.caupanharm.backend.models.caupanharm.valorant.database.PostGresCompsStatsComputed
import perso.caupanharm.backend.models.riot.RawMatch
import perso.caupanharm.backend.models.riot.RawMatchHistory
import perso.caupanharm.backend.models.riot.RiotMatchFull
import perso.caupanharm.backend.models.riot.assets.Agents
import perso.caupanharm.backend.models.riot.assets.Maps
import perso.caupanharm.backend.repositories.AgentsStatsRepository
import perso.caupanharm.backend.repositories.CompsStatsRepository
import perso.caupanharm.backend.repositories.MatchXSAgentRepository
import perso.caupanharm.backend.repositories.MatchXSRepository
import perso.caupanharm.backend.services.HenrikService
import reactor.core.publisher.Mono
import java.time.Instant

private val logger = KotlinLogging.logger {}
@RestController
@RequestMapping("/api/stats")
class StatsController(
        private val henrikService: HenrikService,
        private val alphaNumericalRegex: Regex = "^[a-zA-Z0-9]+$".toRegex(),
        private val usernameRegex: Regex = "^[^,;*\\-#]+#[^,;*\\-#]+\$".toRegex()
) {
    @Autowired
    lateinit var agentsStatsRepository: AgentsStatsRepository

    @Autowired
    lateinit var compsStatsRepository: CompsStatsRepository

    @Autowired
    lateinit var matchXSRepository: MatchXSRepository

    @Autowired
    lateinit var matchXSAgentRepository: MatchXSAgentRepository

    @Value("\${valorant.current.maps}")
    lateinit var mapPool: List<String>

    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Paris") // Adapt cron for testing in dev env if needed
    fun saveMapsAgentsStats() {
        logger.info("Called saveMapsStats")
        val computedStats = mutableListOf<PostGresAgentsStats>()

        val allMaps = matchXSRepository.getMapRates(mapPool)
        val allMapsStats = PostGresAgentsStats(
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
                PostGresAgentsStats(
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
            val currentMapStats = PostGresAgentsStats(
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
                    PostGresAgentsStats(
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

        agentsStatsRepository.deleteAll()
        agentsStatsRepository.saveAll(computedStats)
        logger.info("Maps stats updated")
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Paris")
    fun saveTopComps() {
        val computedComps: MutableList<PostGresCompsStats> = mutableListOf()
        val mapsIncludingAll: MutableList<String?> = mutableListOf(null)
        mapPool.forEach { mapsIncludingAll.add(it) }

        for (map in mapsIncludingAll) {
            val currentMapCompsResponse = getCompsCustom(map, null, "bayesian", 50)
            if (currentMapCompsResponse.statusCode == 200) {
                val currentComps = (currentMapCompsResponse.body as CompStatsResponse).copy(
                    matchingComps = currentMapCompsResponse.body.matchingComps.take(100)
                ).matchingComps
                for (comp in currentComps) {
                    computedComps.add(
                        PostGresCompsStats(
                            map = map,
                            composition = comp.comp,
                            bayesianAverage = comp.bayesianAverage,
                            gamesPlayed = comp.timesPlayed,
                            playRate = comp.pickRateInAllGames,
                            pickRate = comp.pickRateInMatchingComps,
                            winRate = comp.winRate
                        )
                    )
                }
            }
        }

        compsStatsRepository.deleteAll()
        compsStatsRepository.saveAll(computedComps)
        logger.info("Comps stats updated")
    }

    @GetMapping("/agents")
    fun getMapsAgentsStats(): Mono<CaupanharmResponse> {
        logger.info("Endpoint fetched: stats/agents")
        val data = agentsStatsRepository.getData()
        val formattedData = mutableListOf<MapStats>()

        // Global data (no specific agent)
        val allMapsStats = data.first { it.map == null }
        formattedData.add(
            MapStats(
                null,
                allMapsStats.gamesPlayed,
                playRate = null,
                allMapsStats.atkWinRate,
                allMapsStats.defWinRate,
                mutableListOf()
            )
        )

        // Single map data (no specific agent)
        mapPool.forEach { map ->
            val mapStats = data.first { it.map == map && it.agent == null }
            formattedData.add(
                MapStats(
                    map,
                    mapStats.gamesPlayed,
                    mapStats.playRate,
                    mapStats.atkWinRate,
                    mapStats.defWinRate,
                    mutableListOf()
                )
            )
        }

        // Global data (by agent)
        data.filter { row -> row.map == null && row.agent != null }.forEach { row ->
            formattedData.first { it.name == null }.topAgents.add(
                MapStatsAgents(
                    row.agent!!,
                    row.gamesPlayed,
                    row.playRate,
                    row.pickRate!!,
                    row.winRate!!,
                    row.atkWinRate,
                    row.defWinRate
                )
            )
        }

        // Single map data (by agent)
        data.filter { row -> row.map != null && row.agent != null }.forEach { row ->
            formattedData.first { it.name == row.map }.topAgents.add(
                MapStatsAgents(
                    row.agent!!,
                    row.gamesPlayed,
                    row.playRate,
                    row.pickRate!!,
                    row.winRate!!,
                    row.atkWinRate,
                    row.defWinRate
                )
            )
        }

        formattedData.forEach { map -> map.topAgents.sortByDescending { it.winrate } }

        return Mono.just(CaupanharmResponse(200, null, CaupanharmResponseType.AGENTS_STATS, formattedData))
    }

    @GetMapping("/comps")
    fun getTopComps(@RequestParam("map") map: String?, @RequestParam("agents") agentsParam: String?, @RequestParam("sort") sortType: String = "bayesian", @RequestParam("minCount") minCountParam: Int?): Mono<CaupanharmResponse> {
        logger.info("Endpoint fetched: stats/comps")
        val data = compsStatsRepository.getData()
        val mapsIncludingAll: MutableList<String?> = mutableListOf(null)
        mapPool.forEach { mapsIncludingAll.add(it) }

        val computedData: MutableMap<String?, List<PostGresCompsStatsComputed>> = mutableMapOf()

        for (map in mapsIncludingAll) {
            val comps = data.filter { it.map == map }.sortedByDescending { it.bayesianAverage }.map {
                PostGresCompsStatsComputed(
                    composition = it.composition,
                    gamesPlayed = it.gamesPlayed,
                    playRate = it.playRate,
                    pickRate = it.pickRate,
                    winRate = it.winRate
                )
            }

            computedData[map?: "All"] = comps
        }

        return Mono.just(CaupanharmResponse(200, null, CaupanharmResponseType.COMPS_STATS, computedData))
    }

    @GetMapping("/comp")
    fun getCompsCustom(@RequestParam("map") map: String?, @RequestParam("agents") agentsParam: String?, @RequestParam("sort") sortType: String = "bayesian", @RequestParam("minCount") minCountParam: Int?): CaupanharmResponse {
        logger.info("Endpoint fetched: comps with params: map=$map, agents=$agentsParam, sortType=$sortType, minCount=$minCountParam")
        val requestedAgents = agentsParam?.split(',') ?: emptyList()
        if (map != null && Maps.entries.find { it.displayName == map } == null) {
            return CaupanharmResponse(500, "Invalid parameter", CaupanharmResponseType.EXCEPTION, "map")
        }
        requestedAgents.forEach { agent ->
            if (Agents.entries.find { it.displayName == agent } == null) {
                return CaupanharmResponse(500, "Invalid parameter", CaupanharmResponseType.EXCEPTION, "agents")
            }
        }
        if (!alphaNumericalRegex.matches(sortType)) {
            return CaupanharmResponse(500, "Invalid parameter", CaupanharmResponseType.EXCEPTION, "sortType")
        }

        val totalMatchesSaved = matchXSRepository.getMatchesAmount(map)
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

        return CaupanharmResponse(200, null, CaupanharmResponseType.COMP_STATS, compStatsResponse)
    }

    // Using synchronous calls here as this endpoint should later be integrated to another server and not used as an endpoint in Caupanharm
    @GetMapping("populateDatabase")
    fun populateDatabase(@RequestParam("seed") seed: String): CaupanharmResponse {
        if (!usernameRegex.matches(seed)) {
            return CaupanharmResponse(500, "Invalid parameters", CaupanharmResponseType.EXCEPTION, "seed")
        }
        val playerResponse = henrikService.getPlayerFromName(seed).block()!!
        Thread.sleep(2200)
        if (playerResponse.statusCode != 200) return playerResponse
        val player = playerResponse.body as CaupanharmPlayer
        val visitedPlayers: MutableSet<String> = HashSet()
        val playersToVisit: MutableSet<String> = HashSet()
        playersToVisit.add(player.puuid)

        logger.info("Starting populating database with seed $seed")
        return populateDatabaseRecursive(player.puuid, player.region, "competitive", visitedPlayers, playersToVisit)
    }

    // 2s delay after each Henrik request to avoid reaching rate limit
    fun populateDatabaseRecursive(currentPlayer: String, region: String, queue: String, visitedPlayers: MutableSet<String>, playersToVisit: MutableSet<String>): CaupanharmResponse {
        try {
            /*if(Instant.now().atZone(ZoneOffset.UTC).hour >= 2 && Instant.now().atZone(ZoneOffset.UTC).minute >= 15){
                return CaupanharmResponse(200, "Done", CaupanharmResponseType.EXCEPTION, "Populating ended because it is past 22:45 UTC, will restart at 23 UTC.")
            }*/
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
                val foundMatches: MutableList<RawMatch> =
                    firstResponse.history.toMutableList() // Keeping it as a list instead of a set to stop iterating once a given date is reached, since matches are chronologically ordered
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
            return CaupanharmResponse(200, "Done", CaupanharmResponseType.EXCEPTION, null)
        } catch (e: Exception) {
            return CaupanharmResponse(500, null, CaupanharmResponseType.EXCEPTION, e.stackTraceToString())
        }
    }

}