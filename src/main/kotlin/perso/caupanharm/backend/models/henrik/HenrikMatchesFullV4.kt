package perso.caupanharm.backend.models.henrik

data class HenrikMatchesFullV4(
    val status: Int,
    val data: List<HenrikMatchV4>
)

data class HenrikMatchResponseV4(
    val status: Int,
    val data: HenrikMatchV4
)

data class HenrikMatchV4(
    val metadata: HenrikMetadata,
    val players: List<HenrikPlayer>,
    val observers: List<HenrikObserver>,
    val coaches: List<HenrikCoach>,
    val teams: List<HenrikTeam>,
    val rounds: List<HenrikRound>,
    val kills: List<HenrikKillEvents>
)

// Use of underscores defined by the Henrik API, not me
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
    val grenade: Int,
    val ability1: Int,
    val ability2: Int,
    val ultimate: Int
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