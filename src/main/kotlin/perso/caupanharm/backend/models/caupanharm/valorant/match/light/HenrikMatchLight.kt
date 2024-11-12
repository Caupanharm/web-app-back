package perso.caupanharm.backend.models.caupanharm.valorant.match.light

import perso.caupanharm.backend.Utils
import perso.caupanharm.backend.models.caupanharm.CaupanharmResponse


data class HenrikMatchLight(
    val meta: HenrikMatchLightMeta,
    val stats: HenrikMatchLightStats,
    val teams: HenrikMatchLightTeams,
){
    fun toCaupanharmMatchLight(): CaupanharmMatchLight {
        return CaupanharmMatchLight(
            metadata = CaupanharmMatchLightMetadata(
                id = meta.id,
                map = meta.map.name!!,
                startTime = meta.started_at,
                season = meta.season.short!!,
                region = meta.region,
                cluster = meta.cluster
            ),
            stats = CaupanharmMatchLightStats(
                team = stats.team,
                allyScore = if (stats.team == "Blue") teams.blue ?: 0 else teams.red ?: 0,
                enemyScore = if(stats.team == "Blue") teams.red ?: 0 else teams.blue ?: 0,
                agent = stats.character.name!!,
                tier = stats.tier,
                combatScore = stats.score,
                kills = stats.kills,
                deaths = stats.deaths,
                assists = stats.assists,
                headshots = stats.shots.head,
                bodyshots = stats.shots.body,
                legshots = stats.shots.leg,
                damageDealt = stats.damage.made,
                damageReceived = stats.damage.received
            ),
            formattedStats = CaupanharmMatchLightFormatted(
                gameIssue = Utils.computeGameIssue(stats.team, teams.blue, teams.red),
                kda = Utils.computeKDA(stats.kills, stats.deaths, stats.assists),
                kd = Utils.computeKD(stats.kills, stats.deaths),
                dd = Utils.computeDD(stats.damage.made, stats.damage.received, teams.red ?: 0, teams.blue ?: 0),
                hsp = Utils.computeHSP(stats.shots.head, stats.shots.body, stats.shots.leg),
                bsp = Utils.computeBSP(stats.shots.head, stats.shots.body, stats.shots.leg),
                lsp = Utils.computeLSP(stats.shots.head, stats.shots.body, stats.shots.leg),
                adr = Utils.computeADR(stats.damage.made, teams.red ?: 0, teams.blue ?: 0),
                acs = Utils.computeACS(stats.score, teams.red ?: 0, teams.blue ?: 0)
            )
        )
    }

    fun toCaupanharmResponse(): CaupanharmResponse{
        try {
            val caupanharmMatchLight = toCaupanharmMatchLight()
            return CaupanharmResponse(200, null, bodyType = "matchLight", caupanharmMatchLight)
        } catch (e: Exception) {
            return CaupanharmResponse(500, null, bodyType = "exception", e)
        }
    }
}

data class HenrikMatchLightMeta(
    val id: String,
    val map: HenrikMatchLightMetaMap,
    val version: String,
    val mode: String,
    val started_at: String,
    val season: HenrikMatchLightMetaSeason,
    val region: String,
    val cluster: String
)

data class HenrikMatchLightMetaMap(
    val id: String,
    val name: String?
)

data class HenrikMatchLightMetaSeason(
    val id: String,
    val short: String?
)

data class HenrikMatchLightStats(
    val puuid: String,
    val team: String,
    val level: Int,
    val character: HenrikMatchLightStatsCharacter,
    val tier: Int,
    val score: Int,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val shots: HenrikMatchLightStatsShots,
    val damage: HenrikMatchLightStatsDamage
)

data class HenrikMatchLightStatsCharacter(
    val id: String,
    val name: String?
)

data class HenrikMatchLightStatsShots(
    val head: Int,
    val body: Int,
    val leg: Int
)

data class HenrikMatchLightStatsDamage(
    val made: Int,
    val received: Int
)

data class HenrikMatchLightTeams(
    val red: Int?,
    val blue: Int?
)
