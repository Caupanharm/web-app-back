package perso.caupanharm.backend

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import perso.caupanharm.backend.models.caupanharm.*
import perso.caupanharm.backend.models.henrik.*


public class Utils {

    companion object {
        val objectMapper = jacksonObjectMapper()

        fun henrikToCaupanharmMatch(match: HenrikMatchResponseV4): CaupanharmMatch {
            return CaupanharmMatch(
                getMatchInfo(match.data.metadata),
                getPlayers(match.data.players),
                getTeams(match.data.teams),
                getRounds(match.data.rounds),
                getKills(match.data.kills)
            )
        }

        fun caupanharmToPostgresMatch(match: CaupanharmMatch) : PostGresMatch{
            return PostGresMatch(
                match.matchInfo.matchId,
                match.matchInfo.map,
                match.matchInfo.gameLengthMillis,
                match.matchInfo.gameStart,
                match.matchInfo.queue,
                match.matchInfo.season,
                objectMapper.writeValueAsString(match.players),
                objectMapper.writeValueAsString(match.teams),
                objectMapper.writeValueAsString(match.rounds),
                objectMapper.writeValueAsString(match.kills)
                )
        }

        fun postgresToCaupanharmMatch(match: PostGresMatch): CaupanharmMatch{
            return CaupanharmMatch(
                MatchInfo(
                    match.matchId,
                    match.map,
                    match.gameLengthMillis,
                    match.gameStart,
                    true,
                    match.queue,
                    match.season
                ),
                objectMapper.readValue(match.players),
                objectMapper.readValue(match.teams),
                objectMapper.readValue(match.rounds),
                objectMapper.readValue(match.kills)
            )
        }
        

        private fun getMatchInfo(data: HenrikMetadata): MatchInfo {
            return MatchInfo(
                data.match_id,
                data.map.name,
                data.game_length_in_ms,
                data.started_at,
                data.is_completed,
                data.queue.name,
                data.season.short
            )
        }

        private fun getPlayers(players: List<HenrikPlayer>): List<CaupanharmPlayer> {
            val caupanharmPlayers = mutableListOf<CaupanharmPlayer>()
            players.forEach { player ->
                caupanharmPlayers.add(
                    CaupanharmPlayer(
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
                            player.stats.damage?.dealt ?: 0,
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

        private fun getTeams(teams: List<HenrikTeam>): List<CaupanharmTeam> {
            val caupanharmTeams = mutableListOf<CaupanharmTeam>()
            teams.forEach { team ->
                caupanharmTeams.add(
                    CaupanharmTeam(
                        team.team_id,
                        team.rounds.won,
                        team.rounds.lost
                    )
                )
            }
            return caupanharmTeams
        }

        private fun getRounds(rounds: List<HenrikRound>): List<CaupanharmRound> {
            val caupanharmRounds = mutableListOf<CaupanharmRound>()
            rounds.forEach { round ->
                caupanharmRounds.add(
                    CaupanharmRound(
                        round.winning_team,
                        round.result,
                        round.ceremony,
                        round.plant?.let { plantEvent -> getRoundPlantEvent(plantEvent) },
                        round.defuse?.let { defuseEvent -> getRoundDefuseEvent(defuseEvent) },
                        getRoundPlayerStats(round.stats)
                    )
                )
            }
            return caupanharmRounds
        }

        private fun getRoundPlantEvent(event: HenrikPlantEvents): BombEvent {
            return BombEvent(
                event.round_time_in_ms,
                event.site,
                event.location?.let { location -> Location(location.x, location.y) },
                event.player.name + "#" + event.player.tag,
                event.player_locations?.let { getPlayersLocation(event.player_locations) }
            )
        }

        private fun getRoundDefuseEvent(event: HenrikDefuseEvents): BombEvent {
            return BombEvent(
                event.round_time_in_ms,
                null,
                event.location?.let { location -> Location(location.x, location.y) },
                event.player.name + "#" + event.player.tag,
                event.player_locations?.let {
                    getPlayersLocation(event.player_locations)
                }
            )
        }

        private fun getRoundPlayerStats(stats: List<HenrikPlayerStats>): List<RoundPlayerStats> {
            val playerStats = mutableListOf<RoundPlayerStats>()
            stats.forEach { player ->
                playerStats.add(
                    RoundPlayerStats(
                        player.player.name + "#" + player.player.tag,
                        CaupanharmAbilities(
                            player.ability_casts.ability_1 ?: 0,
                            player.ability_casts.ability_2 ?: 0,
                            player.ability_casts.grenade ?: 0,
                            player.ability_casts.ultimate ?: 0
                        ),
                        getDamageEvents(player.damage_events),
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

        private fun getDamageEvents(events: List<HenrikDamageEvents>): List<DamageEvent> {
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

        private fun getKills(kills: List<HenrikKillEvents>): List<CaupanharmKill> {
            val caupanharmKills = mutableListOf<CaupanharmKill>()
            kills.forEach { kill ->
                caupanharmKills.add(
                    CaupanharmKill(
                        kill.round,
                        kill.time_in_round_in_ms,
                        kill.time_in_match_in_ms,
                        kill.killer.name + "#" + kill.killer.tag,
                        kill.victim.name + "#" + kill.victim.tag,
                        kill.assistants.map { assistant -> assistant.name + "#" + assistant.tag },
                        kill.location?.let { location -> Location(location.x, location.y) },
                        kill.weapon.let { weapon -> weapon.name ?: weapon.type },
                        kill.secondary_fire_mode,
                        getPlayersLocation(kill.player_locations)
                    )
                )
            }
            return caupanharmKills
        }

        private fun getPlayersLocation(playersLocation: List<HenrikPlayerLocation>): List<PlayerLocation> {
            return playersLocation.map { playerIt ->
                PlayerLocation(
                    playerIt.player.name + "#" + playerIt.player.tag,
                    playerIt.location?.let { location -> Location(location.x, location.y) },
                    playerIt.view_radians
                )
            }
        }
    }
}
