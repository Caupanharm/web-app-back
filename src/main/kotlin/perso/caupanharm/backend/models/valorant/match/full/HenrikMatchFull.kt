package perso.caupanharm.backend.models.valorant.match.full

import perso.caupanharm.backend.models.caupanharm.CaupanharmResponse

data class HenrikMatchFull(
    val status: Int,
    val data: HenrikMatchFullData
){
    fun toCaupanharmMatchFull(): CaupanharmMatchFull{
        return CaupanharmMatchFull(
            toCaupanharmMatchMetadata(),
            toCaupanharmMatchPlayers(),
            toCaupanharmMatchTeams(),
            toCaupanharmMatchRounds(),
            toCaupanharmMatchKills()
        )
    }

    fun toCaupanharmResponse(): CaupanharmResponse {
        try {
            val caupanharmMatchFull = toCaupanharmMatchFull()
            return CaupanharmResponse(200, null, bodyType = "matchFull", caupanharmMatchFull)
        } catch (e: Exception) {
            return CaupanharmResponse(500, null, bodyType = "exception", e)
        }
    }

    fun toCaupanharmMatchMetadata(): CaupanharmMatchMetadata{
        return CaupanharmMatchMetadata(
            data.metadata.match_id,
            data.metadata.map.name,
            data.metadata.game_length_in_ms,
            data.metadata.started_at,
            data.metadata.is_completed,
            data.metadata.queue.name,
            data.metadata.season.short
        )
    }

    fun toCaupanharmMatchPlayers(): List<CaupanharmMatchPlayer>{
        val caupanharmPlayers = mutableListOf<CaupanharmMatchPlayer>()
        data.players.forEach { player ->
            caupanharmPlayers.add(
                CaupanharmMatchPlayer(
                    player.puuid,
                    player.name + "#" + player.tag,
                    player.team_id,
                    player.party_id,
                    player.tier.name,
                    player.agent.name,
                    CaupanharmPlayerStats(
                        player.stats.score,
                        player.stats.kills,
                        player.stats.deaths ?: 0,
                        player.stats.assists ?: 0,
                        player.stats.headshots ?: 0,
                        player.stats.bodyshots ?: 0,
                        player.stats.legshots ?: 0,
                        player.stats.damage?.made ?: 0,
                        player.stats.damage?.received ?: 0
                    ),
                    CaupanharmAbilities(
                        player.ability_casts.ability1,
                        player.ability_casts.ability2,
                        player.ability_casts.grenade,
                        player.ability_casts.ultimate
                    ),
                    BehaviorSummary(
                        player.behavior.afk_rounds,
                        player.behavior.friendly_fire.outgoing,
                        player.behavior.friendly_fire.incoming,
                        player.behavior.rounds_in_spawn
                    ),
                    TotalEconomy(
                        player.economy.spent.overall,
                        player.economy.loadout_value.overall
                    ),
                )
            )
        }

        return caupanharmPlayers
    }

    fun toCaupanharmMatchTeams(): List<CaupanharmMatchTeam>{
        val caupanharmTeams = mutableListOf<CaupanharmMatchTeam>()
        data.teams.forEach { team ->
            caupanharmTeams.add(
                CaupanharmMatchTeam(
                    team.team_id,
                    team.rounds.won,
                    team.rounds.lost
                )
            )
        }
        return caupanharmTeams
    }

    fun toCaupanharmMatchRounds(): List<CaupanharmMatchRound>{
        val caupanharmRounds = mutableListOf<CaupanharmMatchRound>()
        data.rounds.forEach { round ->
            caupanharmRounds.add(
                CaupanharmMatchRound(
                    round.winning_team,
                    round.result,
                    round.ceremony,
                    round.plant?.let { toRoundPlantEvent(round.plant) },
                    round.defuse?.let { toRoundDefuseEvent(round.defuse) },
                    toRoundPlayerStats(round.stats)
                )
            )
        }
        return caupanharmRounds
    }

    fun toRoundPlantEvent(event: HenrikPlantEvents): BombEvent{
        return BombEvent(
            event.round_time_in_ms,
            event.site,
            event.location?.let { location -> Location(location.x, location.y) },
            event.player.name + "#" + event.player.tag,
            event.player_locations?.let { toPlayersLocation(event.player_locations) }
        )
    }

    fun toRoundDefuseEvent(event: HenrikDefuseEvents): BombEvent{
        return BombEvent(
            event.round_time_in_ms,
            null,
            event.location?.let { location -> Location(location.x, location.y) },
            event.player.name + "#" + event.player.tag,
            event.player_locations?.let { toPlayersLocation(event.player_locations) }
        )
    }

    fun toPlayersLocation(playersLocation: List<HenrikPlayerLocation>): List<PlayerLocation>{
        return playersLocation.map { playerIt ->
            PlayerLocation(
                playerIt.player.name + "#" + playerIt.player.tag,
                playerIt.location?.let { location ->
                    Location(
                        location.x,
                        location.y
                    )
                },
                playerIt.view_radians
            )
        }
    }

    fun toRoundPlayerStats(stats: List<HenrikPlayerStats>): List<RoundPlayerStats>{
        val playerStats = mutableListOf<RoundPlayerStats>()
        stats.forEach { player ->
            playerStats.add(
                RoundPlayerStats(
                    player.player.name + "#" + player.player.tag,
                    CaupanharmAbilities(
                        player.ability_casts.ability_1,
                        player.ability_casts.ability_2,
                        player.ability_casts.grenade,
                        player.ability_casts.ultimate
                    ),
                    toDamageEvents(player.damage_events),
                    RoundPlayerStatsRecap(
                        player.stats.score,
                        player.stats.kills,
                        player.stats.headshots ?: 0,
                        player.stats.bodyshots ?: 0,
                        player.stats.legshots ?: 0,
                    ),
                    RoundEconomy(
                        player.economy.loadout_value,
                        player.economy.remaining,
                        player.economy.weapon?.name,
                        player.economy.armor?.name
                    ),
                    RoundBehavior(
                        player.was_afk,
                        player.received_penalty,
                        player.stayed_in_spawn
                    )
                )
            )
        }
        return playerStats
    }

    fun toDamageEvents(events: List<HenrikDamageEvents>): List<DamageEvent>{
        return events.map { event ->
            DamageEvent(
                event.player.name + "#" + event.player.tag,
                event.damage,
                event.headshots,
                event.bodyshots,
                event.legshots
            )
        }
    }

    fun toCaupanharmMatchKills(): List<CaupanharmMatchKill>{
        val caupanharmKills = mutableListOf<CaupanharmMatchKill>()
        data.kills.forEach { kill ->
            caupanharmKills.add(
                CaupanharmMatchKill(
                    kill.round,
                    kill.time_in_round_in_ms,
                    kill.time_in_match_in_ms,
                    kill.killer.name + "#" + kill.killer.tag,
                    kill.victim.name + "#" + kill.victim.tag,
                    kill.assistants.map { assistant -> assistant.name + "#" + assistant.tag },
                    kill.location?.let { location -> Location(location.x, location.y) },
                    kill.weapon.let { weapon -> weapon.name ?: weapon.type },
                    kill.secondary_fire_mode,
                    toPlayersLocation(kill.player_locations)
                )
            )
        }
        return caupanharmKills
    }
}

data class HenrikMatchFullData(
    val metadata: HenrikMetadata,
    val players: List<HenrikPlayer>,
    val observers: List<HenrikObserver>,
    val coaches: List<HenrikCoach>,
    val teams: List<HenrikTeam>,
    val rounds: List<HenrikRound>,
    val kills: List<HenrikKillEvents>
)

data class HenrikMetadata(
    val match_id: String,
    val map: HenrikMap,
    val game_version: String,
    val game_length_in_ms: Int,
    val started_at: String,
    val is_completed: Boolean,
    val queue: HenrikQueue,
    val season: HenrikSeason,
    val platform: String,
    val premier: HenrikPremierInfo?,
    val party_rr_penaltys: List<HenrikPenalty>?,
    val region: String,
    val cluster: String?
)

data class HenrikMap(
    val id: String,
    val name: String
)

data class HenrikQueue(
    val id: String,
    val name: String?,
    val mode_type: String?
)

data class HenrikSeason(
    val id: String,
    val short: String
)

data class HenrikPenalty(
    val party_id: String,
    val penalty: Int
)

data class HenrikPremierInfo(
    val tournament_id: String?,
    val matchup_id: String?
)

data class HenrikPlayer(
    val puuid: String,
    val name: String,
    val tag: String,
    val team_id: String,
    val platform: String,
    val party_id: String,
    val agent: HenrikAgent,
    val stats: HenrikStats,
    val ability_casts: HenrikAbilitiesPlayer,
    val tier: HenrikTier,
    val customization: HenrikCustomization,
    val account_level: Int,
    val session_playtime_in_ms: Int,
    val behavior: HenrikBehavior,
    val economy: HenrikEconomyShort
)

data class HenrikCustomization(
    val card: String?,
    val title: String?,
    val preferred_level_border: String?,
)

data class HenrikTier(
    val id: Int,
    val name: String?
)

data class HenrikAgent(
    val id: String,
    val name: String
)

data class HenrikAssets(
    val card: HenrikPlayerCard,
    val agent: HenrikAgentAssets
)

data class HenrikPlayerCard(
    val small: String,
    val large: String,
    val wide: String,
    val id: String?
)

data class HenrikAgentAssets(
    val small: String,
    val full : String,
    val bust: String,
    val killfeed: String
)

data class HenrikBehavior(
    val afk_rounds: Int,
    val friendly_fire: HenrikFriendlyFire,
    val rounds_in_spawn: Int
)

data class HenrikFriendlyFire(
    val incoming: Int,
    val outgoing: Int
)

data class HenrikStats(
    val score: Int,
    val kills: Int,
    val deaths: Int?,
    val assists: Int?,
    val headshots: Int?,
    val bodyshots: Int?,
    val legshots: Int?,
    val damage: HenrikDamage?
)

data class HenrikDamage(
    val made: Int,
    val received: Int
)

data class HenrikRoundPlayerStats(
    val score: Int,
    val kills: Int,
    val headshots: Int,
    val bodyshots: Int,
    val legshots: Int
)

data class HenrikEconomyShort(
    val spent: HenrikEconomyDetails,
    val loadout_value: HenrikEconomyDetails
)

data class HenrikAbilitiesPlayer(
    val grenade: Int?,
    val ability1: Int?,
    val ability2: Int?,
    val ultimate: Int?
)


// this api is messed up
data class HenrikAbilitiesRound(
    val grenade: Int?,
    val ability_1: Int?,
    val ability_2: Int?,
    val ultimate: Int?
)

data class HenrikEconomyDetails(
    val overall: Int,
    val average: Double
)

data class HenrikObserver(
    val puuid: String,
    val name: String,
    val tag: String,
    val account_level: Int,
    val session_playtime_in_ms: Int,
    val card_id: String,
    val title_id: String,
    val party_id: String,
)

data class HenrikCoach(
    val puuid: String,
    val team: String
)

data class HenrikTeam(
    val team_id: String,
    val rounds: HenrikRoundsShort,
    val won: Boolean,
    val premier_roster: HenrikRoster?
)

data class HenrikRoundsShort(
    val won: Int,
    val lost: Int
)


data class HenrikRoster(
    val id: String,
    val members: List<String>,
    val name: String,
    val customization: HenrikIconCustomization,
    val tag: String
)

data class HenrikIconCustomization(
    val icon: String,
    val image: String,
    val primary_color: String,
    val secondary_color: String,
    val tertiary_color: String
)

data class HenrikRound(
    val id: Int,
    val result: String,
    val ceremony: String,
    val winning_team: String,
    val plant: HenrikPlantEvents?,
    val defuse: HenrikDefuseEvents?,
    val stats: List<HenrikPlayerStats>
)

data class HenrikPlantEvents(
    val round_time_in_ms: Int,
    val site: String,
    val location: HenrikLocation?,
    val player: HenrikPlayerShort,
    val player_locations: List<HenrikPlayerLocation>?,
)


data class HenrikDefuseEvents(
    val round_time_in_ms: Int,
    val location: HenrikLocation?,
    val player: HenrikPlayerShort,
    val player_locations: List<HenrikPlayerLocation>?,
)

data class HenrikLocation(
    val x: Int,
    val y: Int
)

data class HenrikPlayerShort(
    val puuid: String,
    val name: String,
    val tag: String,
    val team: String
)

data class HenrikPlayerLocation(
    val player: HenrikPlayerShort,
    val view_radians: Double,
    val location: HenrikLocation?
)

data class HenrikPlayerStats(
    val player: HenrikPlayerShort,
    val ability_casts: HenrikAbilitiesRound,
    val damage_events: List<HenrikDamageEvents>,
    val stats: HenrikStats,
    val economy: HenrikEconomy,
    val was_afk: Boolean,
    val received_penalty: Boolean,
    val stayed_in_spawn: Boolean
)

data class HenrikDamageEvents(
    val player: HenrikPlayerShort,
    val damage: Int,
    val headshots: Int,
    val bodyshots: Int,
    val legshots: Int
)

data class HenrikEconomy(
    val loadout_value: Int,
    val remaining: Int,
    val weapon: HenrikWeapon?,
    val armor: HenrikArmor?,

    )

data class HenrikArmor(
    val id: String,
    val name: String
)

data class HenrikKillEvents(
    val round: Int,
    val time_in_round_in_ms: Int,
    val time_in_match_in_ms: Int,
    val killer: HenrikPlayerShort,
    val victim: HenrikPlayerShort,
    val assistants: List<HenrikPlayerShort>,
    val location: HenrikLocation?,
    val weapon: HenrikWeapon,
    val secondary_fire_mode: Boolean,
    val player_locations: List<HenrikPlayerLocation>
)

data class HenrikWeapon(
    val id: String,
    val name: String?,
    val type: String?
)
