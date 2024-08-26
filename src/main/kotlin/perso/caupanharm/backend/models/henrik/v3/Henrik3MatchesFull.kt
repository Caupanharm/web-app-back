package perso.caupanharm.backend.models.henrik.v3

import perso.caupanharm.backend.models.henrik.*

data class Henrik3MatchesV3(
    val status: Int,
    val data: List<MatchV3>
)

data class Henrik3MatchV3(
    val status: Int,
    val data: MatchV3
)

data class MatchV3(
    val metadata: MetadataV3,
    val players: Players,
    val observers: List<Observer>,
    val coaches: List<Coach>,
    val teams: TeamsV3,
    val rounds: List<Round>,
    val kills: List<KillEvents>
)

// Use of underscores defined by the Henrik API, not me
data class MetadataV3(
    val map: String,
    val game_version: String,
    val game_length: Int,
    val game_start: Int,
    val game_start_patched: String,
    val rounds_played: Int,
    val mode: String,
    val mode_id: String,
    val queue: String,
    val season_id: String,
    val platform: String,
    val matchid: String,
    val premier_info: PremierInfo,
    val region: String,
    val cluster: String
)

data class PremierInfo(
    val tournament_id: String?,
    val matchup_id: String?
)


data class Players(
    val all_players: List<Player>,
    val red: List<Player>,
    val blue: List<Player>
)

data class Player(
    val puuid: String,
    val name: String,
    val tag: String,
    val team: String,
    val level: Int,
    val character: String,
    val currenttier: Int,
    val currenttier_patched: String,
    val player_card: String,
    val player_title: String,
    val party_id: String,
    val session_playtime: HenrikSession,
    val assets: Assets,
    val behavior: Behavior,
    val platform: HenrikPlatform,
    val ability_casts: HenrikAbilitiesV3,
    val stats: StatsV3,
    val economy: EconomyShort,
    val damage_made: Int,
    val damage_received: Int
)


data class Assets(
    val card: HenrikPlayerCard,
    val agent: HenrikAgentAssets
)

data class Behavior(
    val afk_rounds: Int,
    val friendly_fire: FriendlyFire,
    val rounds_in_spawn: Int
)

data class FriendlyFire(
    val incoming: Int,
    val outgoing: Int
)

data class StatsV3(
    val score: Int,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val bodyshots: Int,
    val headshots: Int,
    val legshots: Int
)

data class EconomyShort(
    val spent: EconomyDetails,
    val loadout_value: EconomyDetails
)

data class EconomyDetails(
    val overall: Int,
    val average: Int
)

data class Observer(
    val puuid: String,
    val name: String,
    val tag: String,
    val platform: HenrikPlatform,
    val session_playtime: HenrikSession,
    val team: String,
    val level: Int,
    val player_card: String,
    val player_tite: String,
    val party_id: String
)

data class Coach(
    val puuid: String,
    val team: String
)

data class TeamsV3(
    val red: TeamV3,
    val blue: TeamV3
)

data class TeamV3(
    val has_won: Boolean?,
    val rounds_won: Int?,
    val rounds_lost: Int?,
    val roaster: Roaster?
)

data class Roaster(
    val members: List<String>,
    val name: String,
    val tag: String,
    val customization: Customization
)

data class Customization(
    val icon: String,
    val image: String,
    val primary: String,
    val secondary: String,
    val tertiary: String
)

data class Round(
    val winning_team: String,
    val end_type: String,
    val bomb_planted: Boolean?,
    val bomb_defused: Boolean?,
    val plant_events: PlantEvents?,
    val defuse_events: DefuseEvents?,
    val player_stats: List<PlayerStats>
)

data class PlantEvents(
    val plant_location: HenrikLocation?,
    val planted_by: HenrikPlayerShortV3?,
    val plant_site: String?,
    val plant_time_in_round: Int?,
    val players_locations_on_plant: List<HenrikPlayerLocationV3>?,
)

data class DefuseEvents(
    val defuse_location: HenrikLocation?,
    val defused_by: HenrikPlayerShortV3?,
    val defuse_time_in_round: Int?,
    val players_locations_on_defuse: List<HenrikPlayerLocationV3>?
)

data class PlayerStats(
    val ability_casts: HenrikAbilitiesV3,
    val player_puuid: String,
    val player_display_name: String,
    val player_team: String,
    val damage_events: List<DamageEvents>,
    val damage: Int,
    val bodyshots: Int,
    val headshots: Int,
    val legshots: Int,
    val kill_events: List<KillEvents>,
    val kills: Int,
    val score: Int,
    val economy: Economy,
    val was_afk: Boolean,
    val was_penalized: Boolean,
    val stayed_in_spawn: Boolean
)

data class DamageEvents(
    val receiver_puuid: String,
    val receiver_display_name: String,
    val receiver_team: String,
    val bodyshots: Int,
    val damage: Int,
    val headshots: Int,
    val legshots: Int
)

data class Economy(
    val loadout_value: Int,
    val weapon: HenrikEquipment,
    val armor: HenrikEquipment,
    val remaining: Int,
    val spent: Int
)

data class KillEvents(
    val kill_time_in_round: Int,
    val kill_time_in_match: Int,
    val killer_puuid: String,
    val killer_display_name: String,
    val killer_team: String,
    val victim_puuid: String,
    val victim_display_name: String,
    val victim_team: String,
    val victim_death_location: HenrikLocation,
    val damage_weapon_id: String,
    val damage_weapon_name: String?,
    val damage_weapon_assets: HenrikEquipmentAssets,
    val secondary_fire_mode: Boolean,
    val player_locations_on_kill: List<HenrikPlayerLocationV3>,
    val assistants: List<Assistant>
)

data class Assistant(
    val assistant_puuid: String,
    val assistant_display_name: String,
    val assistant_team: String
)