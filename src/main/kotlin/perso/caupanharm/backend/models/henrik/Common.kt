package perso.caupanharm.backend.models.henrik

import perso.caupanharm.backend.models.henrik.v3.*


data class PlayerCard(
    val small: String,
    val large: String,
    val wide: String,
    val id: String?
)

data class AgentAssets(
    val small: String,
    val full : String,
    val bust: String,
    val killfeed: String
)

data class Damage(
    val made: Int,
    val received: Int
)

data class Platform(
    val type: String,
    val os: OS
)

data class OS(
    val name: String,
    val version: String
)

data class Session(
    val minutes: Int,
    val seconds: Int,
    val milliseconds: Int
)

data class Location(
    val x: Int,
    val y: Int
)

data class PlayerShortV3(
    val puuid: String,
    val display_name: String,
    val team: String
)

data class PlayerLocationV3(
    val player_puuid: String,
    val player_display_name: String,
    val player_team: String,
    val location: Location,
    val view_radians: Double
)

data class AbilitiesV3(
    val c_cast: Int?,
    val q_cast: Int?,
    val e_cast: Int?,
    val x_cast: Int?
)

data class Equipment(
    val id: String?,
    val name: String?,
    val assets: EquipmentAssets
)

data class EquipmentAssets(
    val display_icon: String?,
    val killfeed_icon: String?
)

data class Results(
    val total: Int,
    val returned: Int,
    val before: Int,
    val after: Int
)

data class V1LifetimeMatchesItem(
    val meta: MetaV1,
    val stats: StatsV1,
    val teams: TeamsV1
)

data class MetaV1(
    val id: String,
    val map: MapInfo,
    val version: String,
    val mode: String,
    val started_at: String,
    val season: Season,
    val region: String,
    val cluster: String
)
data class StatsV1(
    val puuid: String,
    val team: String,
    val level: Int,
    val character: Character,
    val tier: Int,
    val score: Int,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val shots: Shots,
    val damage: Damage
)
data class TeamsV1(
    val red: Int,
    val blue: Int
)

enum class Maps(val formatted: String) {
    ASCENT("Ascent"),
    SPLIT("Split"),
    FRACTURE("Fracture"),
    BIND("Bind"),
    BREEZE("Breeze"),
    DISTRICT("District"),
    KASBAH("Kasbah"),
    PIAZZA("Piazza"),
    LOTUS("Lotus"),
    PEARL("Pearl"),
    ICEBOX("Icebox"),
    HAVEN("Haven")
}

enum class Modes(val formatted: String) {
    COMPETITIVE("Competitive"),
    CUSTOM_GAME("Custom Game"),
    DEATHMATCH("Deathmatch"),
    ESCALATION("Escalation"),
    TEAM_DEATHMATCH("Team Deathmatch"),
    NEW_MAP("New Map"),
    REPLICATION("Replication"),
    SNOWBALL_FIGHT("Snowball Fight"),
    SPIKE_RUSH("Spike Rush"),
    SWIFTPLAY("Swiftplay"),
    UNRATED("Unrated")
}

enum class ModeIds(val formatted:String){
    COMPETITIVE("competitive"),
    CUSTOM_GAME("custom"),
    DEATHMATCH("deathmatch"),
    TEAM_DEATHMATCH("ggteam"),
    ESCALATION("hurm"),
    NEW_MAP("newmap"),
    REPLICATION("onefa"),
    SNOWBALL_FIGHT("snowball"),
    SPIKE_RUSH("spikerush"),
    SWIFTPLAY("swiftplay"),
    UNRATED("unrated")
}

enum class Regions(val formatted: String){
    EUROPE("eu"),
    NORTH_AMERICA("na"),
    ASIA_PACIFIC("ap"),
    KOREA("kr")
}