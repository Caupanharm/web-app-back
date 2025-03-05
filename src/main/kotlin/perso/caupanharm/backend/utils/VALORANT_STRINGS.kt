package perso.caupanharm.backend.utils

import perso.caupanharm.backend.models.riot.assets.Agents

enum class VALORANT_STRINGS(val value: String, val formatted: String) {
    TEAM_BLUE("Blue","Blue"),
    TEAM_RED("Red","Red"),
    ROUND_ENDING_ELIMINATION("Eliminated","Eliminated"),
    ROUND_ENDING_SURRENDER("surrendered","Surrendered"),
    ROUND_ENDING_DEFUSE("bomb defused","Bomb defused"),
    ROUND_ENDING_EXPLODE("bomb detonated","Bomb detonated"),
    BOMB_KILL("Bomb","Bomb"); // TODO to move into the Weapons enum once correctly structured

    companion object {
        fun getFromFormatted(formatted: String): VALORANT_STRINGS {
            return VALORANT_STRINGS.entries.first { it.formatted == formatted }
        }

        fun getFromValue(value: String): VALORANT_STRINGS {
            return VALORANT_STRINGS.entries.first { it.value == value }
        }
    }
}