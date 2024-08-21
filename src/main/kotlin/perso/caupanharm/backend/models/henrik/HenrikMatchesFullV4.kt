package perso.caupanharm.backend.models.henrik

data class HenrikMatchesFullV4(
    val status: Int,
    val data: List<MatchV4>
)

data class HenrikMatchV4(
    val status: Int,
    val data: MatchV4
)

data class MatchV4(
    val metadata: Metadata,
    val players: List<Player>,
    val observers: List<Observer>,
    val coaches: List<Coach>,
    val teams: List<Team>,
    val rounds: List<Round>,
    val kills: List<KillEvents>
)

// Use of underscores defined by the Henrik API, not me
data class Metadata(
    val match_id: String,
    val map: Map,
    val game_version: String,
    val game_length_in_ms: Int,
    val started_at: String,
    val is_completed: Boolean,
    val queue: Queue,
    val season: Season,
    val platform: String,
    val premier_info: PremierInfo?,
    val party_rr_penaltys: List<Penalty>?,
    val region: String,
    val cluster: String?
)

data class Map(
    val id: String,
    val name: String
)

data class Queue(
    val id: String,
    val name: String?,
    val mode_type: String?
)

data class Season(
    val id: String,
    val short: String
)

data class Penalty(
    val party_id: String,
    val penalty: Int
)

data class PremierInfo(
    val tournament_id: String?,
    val matchup_id: String?
)

data class Player(
    val puuid: String,
    val name: String,
    val tag: String,
    val team_id: String,
    val platform: String,
    val party_id: String,
    val agent: Agent,
    val stats: Stats,
    val ability_casts: Abilities,
    val tier: Tier,
    val card_id: String?,
    val title_id: String?,
    val prefered_level_border: String?,
    val account_level: Int,
    val session_playtime_in_ms: Int,
    val behavior: Behavior,
    val economy: EconomyShort
)

data class Tier(
    val id: String,
    val name: String?
)

data class Agent(
    val id: String,
    val name: String
)

data class Assets(
    val card: PlayerCard,
    val agent: AgentAssets
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

data class Stats(
    val score: Int,
    val kills: Int,
    val deaths: Int?,
    val assists: Int,
    val bodyshots: Int,
    val headshots: Int,
    val legshots: Int,
    val damage: Damage?
)

data class EconomyShort(
    val spent: EconomyDetails,
    val loadout_value: EconomyDetails
)

data class Abilities(
    val grenade: Int,
    val ability_1: Int,
    val ability_2: Int,
    val ultimate: Int
)

data class EconomyDetails(
    val overall: Int,
    val average: Int
)

data class Observer(
    val puuid: String,
    val name: String,
    val tag: String,
    val account_level: Int,
    val session_playtime_in_ms: Int,
    val card_id: String,
    val title_id: String,
    val party_id: String,
)

data class Coach(
    val puuid: String,
    val team: String
)

data class Team(
    val team_id: String,
    val rounds: RoundsShort,
    val won: Boolean,
    val premier_roster: Roster?
)

data class RoundsShort(
    val won: Int,
    val lost: Int
)


data class Roster(
    val id: String,
    val members: List<String>,
    val name: String,
    val customization: Customization,
    val tag: String
    )

data class Customization(
    val icon: String,
    val image: String,
    val primary_color: String,
    val secondary_color: String,
    val tertiary_color: String
)

data class Round(
    val id: String,
    val result: String,
    val ceremony: String,
    val winning_team: String,
    val plant: PlantEvents?,
    val defuse: DefuseEvents?,
    val stats: List<PlayerStats>
)

data class PlantEvents(
    val round_time_in_ms: Int,
    val site: String,
    val location: Location?,
    val player: PlayerShort,
    val players_locations: List<PlayerLocation>?,
)


data class DefuseEvents(
    val round_time_in_ms: Int,
    val location: Location?,
    val player: PlayerShort,
    val players_locations: List<PlayerLocation>?,
)


data class PlayerShort(
    val puuid: String,
    val name: String,
    val tag: String,
    val team: String
)

data class PlayerLocation(
    val player: PlayerShort,
    val view_radians: Float,
    val location: Location?
)

data class PlayerStats(
    val ability_casts: Abilities,
    val player: PlayerShort,
    val damage_events: List<DamageEvents>,
    val stats: Stats,
    val economy: Economy,
    val was_afk: Boolean,
    val received_penalty: Boolean,
    val stayed_in_spawn: Boolean
)

data class DamageEvents(
    val player: PlayerShort,
    val bodyshots: Int,
    val damage: Int,
    val headshots: Int,
    val legshots: Int
)

data class Economy(
    val loadout_value: Int,
    val weapon: Weapon?,
    val armor: Armor?,
    val remaining: Int,
)

data class Armor(
    val id: String,
    val name: String
)

data class KillEvents(
    val round: Int,
    val time_in_round_in_ms: Int,
    val time_in_match_in_ms: Int,
    val killer: PlayerShort,
    val victim: PlayerShort,
    val assistants: List<PlayerShort>,
    val location: Location?,
    val weapon: Weapon,
    val secondary_fire_mode: Boolean,
    val player_locations: List<PlayerLocation>
)

data class Weapon(
    val id: String,
    val name: String?,
    val type: String?
)