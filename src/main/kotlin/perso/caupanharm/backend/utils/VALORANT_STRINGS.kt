package perso.caupanharm.backend.utils

import perso.caupanharm.backend.models.riot.assets.Agents

enum class VALORANT_STRINGS(val value: String, val formatted: String) {
    TEAM_BLUE("Blue","Blue"),
    TEAM_RED("Red","Red"),
    ROUND_ENDING_ELIMINATION("Eliminated","Eliminated"),
    ROUND_ENDING_SURRENDER("surrendered","Surrendered"),
    ROUND_ENDING_DEFUSE("bomb defused","Bomb defused"),
    ROUND_ENDING_EXPLODE("bomb detonated","Bomb detonated");

    companion object {
        fun getFromFormatted(value: String): VALORANT_STRINGS? {
            return VALORANT_STRINGS.entries.find { it.value == value }
        }

        fun getFromValue(formatted: String?): VALORANT_STRINGS? {
            return VALORANT_STRINGS.entries.find { it.formatted == formatted }
        }
    }
}