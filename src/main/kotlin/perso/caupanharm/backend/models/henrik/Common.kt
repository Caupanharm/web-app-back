package perso.caupanharm.backend.models.henrik

import perso.caupanharm.backend.models.henrik.v3.*


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

data class HenrikDamage(
    val dealt: Int,
    val received: Int
)

data class HenrikPlatform(
    val type: String,
    val os: HenrikOS
)

data class HenrikOS(
    val name: String,
    val version: String
)

data class HenrikSession(
    val minutes: Int,
    val seconds: Int,
    val milliseconds: Int
)

data class HenrikLocation(
    val x: Int,
    val y: Int
)

data class HenrikPlayerShortV3(
    val puuid: String,
    val display_name: String,
    val team: String
)

data class HenrikPlayerLocationV3(
    val player_puuid: String,
    val player_display_name: String,
    val player_team: String,
    val location: HenrikLocation,
    val view_radians: Double
)

data class HenrikAbilitiesV3(
    val c_cast: Int?,
    val q_cast: Int?,
    val e_cast: Int?,
    val x_cast: Int?
)

data class HenrikEquipment(
    val id: String?,
    val name: String?,
    val assets: HenrikEquipmentAssets
)

data class HenrikEquipmentAssets(
    val display_icon: String?,
    val killfeed_icon: String?
)

data class HenrikResults(
    val total: Int,
    val returned: Int,
    val before: Int,
    val after: Int
)

data class HenrikV1LifetimeMatchesItem(
    val meta: HenrikMetaV1,
    val stats: HenrikStatsV1,
    val teams: HenrikTeamsV1
)

data class HenrikMetaV1(
    val id: String,
    val map: MapInfo,
    val version: String,
    val mode: String,
    val started_at: String,
    val season: HenrikSeason,
    val region: String,
    val cluster: String
)
data class HenrikStatsV1(
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
    val damage: HenrikDamage
)
data class HenrikTeamsV1(
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