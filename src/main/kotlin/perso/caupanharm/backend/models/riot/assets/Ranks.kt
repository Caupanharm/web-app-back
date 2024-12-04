package perso.caupanharm.backend.models.riot.assets

enum class Ranks(val tier: Int, val divisionName: String, val tierName: String) {
    UNRANKED(0, "UNRANKED","UNRANKED"),
    IRON1(3, "IRON","IRON 1"),
    IRON2(4, "IRON","IRON 2"),
    IRON3(5, "IRON","IRON 3"),
    BRONZE1(6, "BRONZE","BRONZE 1"),
    BRONZE2(7, "BRONZE","BRONZE 2"),
    BRONZE3(8, "BRONZE","BRONZE 3"),
    SILVER1(9, "SILVER","SILVER 1"),
    SILVER2(10, "SILVER","SILVER 2"),
    SILVER3(11, "SILVER","SILVER 3"),
    GOLD1(12, "GOLD","GOLD 1"),
    GOLD2(13, "GOLD","GOLD 2"),
    GOLD3(14, "GOLD","GOLD 3"),
    PLATINUM1(15, "PLATINUM","PLATINUM 1"),
    PLATINUM2(16, "PLATINUM","PLATINUM 2"),
    PLATINUM3(17, "PLATINUM","PLATINUM 3"),
    DIAMOND1(18, "DIAMOND","DIAMOND 1"),
    DIAMOND2(19, "DIAMOND","DIAMOND 2"),
    DIAMOND3(20, "DIAMOND","DIAMOND 3"),
    ASCENDANT1(21, "ASCENDANT","ASCENDANT 1"),
    ASCENDANT2(22, "ASCENDANT","ASCENDANT 2"),
    ASCENDANT3(23, "ASCENDANT","ASCENDANT 3"),
    IMMORTAL1(24, "IMMORTAL","IMMORTAL 1"),
    IMMORTAL2(25, "IMMORTAL","IMMORTAL 2"),
    IMMORTAL3(26, "IMMORTAL","IMMORTAL 3"),
    RADIANT(27, "RADIANT","RADIANT");

    companion object{
        fun findByTier(tier: Int): Ranks {
            return entries.find{ it.tier == tier}?: UNRANKED
        }
    }
}