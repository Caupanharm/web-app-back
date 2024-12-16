package perso.caupanharm.backend.models.riot

import perso.caupanharm.backend.models.caupanharm.valorant.match.*
import perso.caupanharm.backend.models.riot.assets.*

data class RiotMatchFull(
    val matchInfo: RiotMatchInfo,
    val players: List<RiotMatchPlayer>,
    val coaches: List<Any>, // to find out
    val teams: List<RiotMatchTeam>,
    val roundResults: List<RiotMatchRoundResult>,
    val kills: List<RiotMatchKill>
) {
    fun toCaupanharmMatchFull(): CaupanharmMatchFull {
        return CaupanharmMatchFull(
            metadata = matchInfo.toCaupanharmMatchMetadata(),
            players = players.map { it.toCaupanharmMatchPlayer() },
            score = createCaupanharmScore(),
            rounds = roundResults.filter{it.roundResult != "Surrendered" }.map { it.toCaupanharmMatchRound() },
            kills = kills.map { it.toCaupanharmMatchKill() }
        )
    }

    private fun createCaupanharmScore(): CaupanharmMatchScore {
        val blue = if (teams[0].teamId == "Blue") teams[0].roundsWon else teams[1].roundsWon
        val red = if (teams[0].teamId == "Red") teams[0].roundsWon else teams[1].roundsWon
        var blueAttack = 0
        var blueDefense = 0
        var redAttack = 0
        var redDefense = 0

        roundResults.forEach { round ->
            when{
                round.roundNum <= 11 || (round.roundNum >= 24 && round.roundNum % 2 == 0) -> {
                    if(round.winningTeam == "Blue"){
                        blueDefense++
                    }else{
                        redAttack++
                    }
                }
                round.roundNum <= 23 || (round.roundNum >= 24 && round.roundNum %2 == 1) -> {
                    if(round.winningTeam == "Blue"){
                        blueAttack++
                    }else{
                        redDefense++
                    }
                }
            }
        }

        return CaupanharmMatchScore(blue, red, blueAttack, blueDefense, redAttack, redDefense)
    }
}

data class RiotMatchInfo(
    val matchId: String,
    val mapId: String,
    val gamePodId: String,
    val gameLoopZone: String,
    val gameServerAddress: String,
    val gameVersion: String,
    val gameLengthMillis: Int,
    val gameStartMillis: Long,
    val provisioningFlowID: String,
    val isCompleted: Boolean,
    val customGameName: String,
    val forcePostProcessing: Boolean,
    val queueID: String,
    val gameMode: String,
    val isRanked: Boolean,
    val isMatchSampled: Boolean,
    val seasonId: String,
    val completionState: String,
    val platformType: String,
    val premierMatchInfo: RiotMatchPremierMatchInfo,
    val partyRRPenalties: Map<String, Int>,
    val shouldMatchDisablePenalties: Boolean
) {
    fun toCaupanharmMatchMetadata(): CaupanharmMatchMetadata {
        return CaupanharmMatchMetadata(
            matchId = matchId,
            map = Maps.getNameFromUuid(mapId),
            gameLengthMillis = gameLengthMillis,
            gameStartMillis = gameStartMillis,
            queue = queueID,
            season = Episodes.getTitleFromUuid(seasonId)
        )
    }
}

data class RiotMatchPremierMatchInfo(
    val premierSeasonId: String?,
    val premierEventId: String?
)

data class RiotMatchPlayer(
    val subject: String,
    val gameName: String,
    val tagLine: String,
    val platformInfo: RiotMatchPlayerPlatformInto,
    val teamId: String,
    val partyId: String,
    val characterId: String,
    val stats: RiotPlayerStats,
    val roundDamage: List<RiotMatchPlayerRoundDamage>?,
    val competitiveTier: Int,
    val isObserver: Boolean,
    val playerCard: String,
    val playerTitle: String,
    val preferredLevelBorder: Any?, // to find out
    val accountLevel: Int,
    val sessionPlaytimeMinutes: Int,
    val xpModifications: Any?, // to find out
    val behaviorFactors: RiotMatchPlayerBehaviorFactor,
    val newPlayerExperienceDetails: RiotMatchPlayerExperience
) {
    fun toCaupanharmMatchPlayer(): CaupanharmMatchPlayer {
        return CaupanharmMatchPlayer(
            playerId = subject,
            name = "$gameName#$tagLine",
            team = teamId,
            party = partyId,
            rank = competitiveTier,
            agent = Agents.getTitleFromUuid(characterId),
            stats = stats.toCaupanharmPlayerStats(),
            abilityCasts = stats.abilityCasts.toCaupanharmAbilities(),
            behavior = behaviorFactors.toCaupanharmBehavior()
        )
    }
}

data class RiotMatchPlayerPlatformInto(
    val platformType: String,
    val platformOS: String,
    val platformOSVersion: String,
    val platformChipset: String,
    val platformDevice: String
)

data class RiotPlayerStats(
    val score: Int,
    val roundsPlayed: Int,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val playtimeMillis: Int,
    val abilityCasts: RiotMatchPlayerAbilityCasts
) {
    fun toCaupanharmPlayerStats(): CaupanharmPlayerStats {
        return CaupanharmPlayerStats(
            score = score,
            kills = kills,
            deaths = deaths,
            assists = assists
        )
    }
}

data class RiotMatchPlayerAbilityCasts(
    val grenadeCasts: Int?,
    val ability1Casts: Int?,
    val ability2Casts: Int?,
    val ultimateCasts: Int?
) {
    fun toCaupanharmAbilities(): CaupanharmAbilities {
        return CaupanharmAbilities(
            ability1 = ability1Casts,
            ability2 = ability2Casts,
            ability3 = grenadeCasts,
            ultimate = ultimateCasts
        )
    }
}

data class RiotMatchPlayerRoundDamage(
    val round: Int,
    val receiver: String,
    val damage: Int
)

data class RiotMatchPlayerBehaviorFactor(
    val afkRounds: Int,
    val collisions: Double,
    val commsRatingRecovery: Int,
    val damageParticipationOutgoing: Int,
    val friendlyFireIncoming: Int,
    val friendlyFireOutgoing: Int,
    val mouseMovement: Double,
    val selfDamage: Int,
    val stayedInSpawnRounds: Int
) {
    fun toCaupanharmBehavior(): BehaviorSummary {
        return BehaviorSummary(
            afk = afkRounds,
            dealtFriendlyFire = friendlyFireOutgoing,
            inSpawn = stayedInSpawnRounds
        )
    }
}

data class RiotMatchPlayerExperience(
    val basicMovement: RiotMatchPlayerExperienceDetails,
    val basicGunSkill: RiotMatchPlayerExperienceDetails,
    val adaptiveBots: RiotMatchPlayerExperienceDetails,
    val ability: RiotMatchPlayerExperienceDetails,
    val bombPlant: RiotMatchPlayerExperienceDetails,
    val defendBombSite: RiotMatchPlayerExperienceDetails,
    val settingStatus: RiotMatchPlayerExperienceDetails,
    val versionString: String
)

data class RiotMatchPlayerExperienceDetails(
    val idleTimeMillis: Int,
    val objectiveCompleteTimeMillis: Int,
    val adaptativeBotAverageDurationMillisAllAttemps: Int?,
    val adaptativeBotAverageDurationMillisFirstAttempt: Int?,
    val killDetailsFirstAttempt: Any?, // to find out
    val success: Boolean?,
    val isMouseSensitivityDefault: Boolean?,
    val isCrosshairDefault: Boolean?,
)

data class RiotMatchTeam(
    val teamId: String,
    val won: Boolean,
    val roundsPlayed: Int,
    val roundsWon: Int,
    val numPoints: Int,
    val rosterInfo: Any? // To find out
)

data class RiotMatchRoundResult(
    val roundNum: Int,
    val roundResult: String,
    val roundCeremony: String,
    val winningTeam: String,
    val bombPlanter: String?,
    val bombDefuser: String?,
    val plantRoundTime: Int,
    val plantPlayerLocations: List<RiotPlayerLocation>?,
    val plantLocation: RiotLocation?,
    val plantSite: String?,
    val defuseRoundTime: Int,
    val defusePlayerLocations: List<RiotPlayerLocation>?,
    val defuseLocation: RiotLocation?,
    val playerStats: List<RiotRoundPlayerStats>,
    val roundResultCode: String,
    val playerEconomies: List<RiotRoundPlayerEconomy>?,
    val playerScores: List<RiotRoundPlayerScore>?
) {
    fun toCaupanharmMatchRound(): CaupanharmMatchRound {
        return CaupanharmMatchRound(
            winningTeam = winningTeam,
            result = roundResult,
            ceremony = Ceremonies.getCustomNameFromDisplayName(roundCeremony),
            plantEvent = getPlantEvent(),
            defuseEvent = getDefuseEvent(),
            stats = playerStats.map { it.toCaupanharmRoundPlayerStats() }
        )

    }

    private fun getPlantEvent(): BombEvent? {
        if (bombPlanter == null) return null
        return BombEvent(
            roundTimeMillis = plantRoundTime,
            site = plantSite!!,
            location = plantLocation!!.toCaupanharmLocation(),
            player = bombPlanter,
            playersLocation = plantPlayerLocations?.map { it.toCaupanharmPlayerLocation() }
        )
    }

    private fun getDefuseEvent(): BombEvent? {
        if (bombDefuser == null) return null
        return BombEvent(
            roundTimeMillis = defuseRoundTime,
            site = plantSite!!,
            location = defuseLocation!!.toCaupanharmLocation(),
            player = bombDefuser,
            playersLocation = defusePlayerLocations?.map { it.toCaupanharmPlayerLocation() }
        )
    }
}

data class RiotPlayerLocation(
    val subject: String,
    val viewRadians: Double,
    val location: RiotLocation
) {
    fun toCaupanharmPlayerLocation(): PlayerLocation {
        return PlayerLocation(
            player = subject,
            location = location.toCaupanharmLocation(),
            viewRadians = viewRadians
        )
    }
}

data class RiotLocation(
    val x: Int,
    val y: Int
) {
    fun toCaupanharmLocation(): Location {
        return Location(
            x = x,
            y = y
        )
    }
}

data class RiotRoundPlayerStats(
    val subject: String,
    val kills: List<RiotMatchKill>,
    val damage: List<RiotDamageDetails>,
    val score: Int,
    val economy: RiotEconomy,
    val ability: RiotRoundAbilityCasts,
    val wasAfk: Boolean,
    val wasPenalized: Boolean,
    val stayedInSpawn: Boolean
) {
    fun toCaupanharmRoundPlayerStats(): CaupanharmRoundPlayerStats {
        return CaupanharmRoundPlayerStats(
            player = subject,
            abilityCasts = ability.toCaupanharmAbilities(),
            damageEvents = damage.map { it.toCaupanharmDamageEvent() },
            score = score,
            kills = kills.map{it.toCaupanharmMatchKill()},
            economy = economy.toCaupanharmEconomy(),
            behavior = RoundBehavior(wasAfk, wasPenalized, stayedInSpawn)
        )
    }
}

data class RiotDamageDetails(
    val receiver: String,
    val damage: Int,
    val legshots: Int,
    val bodyshots: Int,
    val headshots: Int
) {
    fun toCaupanharmDamageEvent(): DamageEvent {
        return DamageEvent(
            player = receiver,
            damage = damage,
            headshots = headshots,
            bodyshots = bodyshots,
            legshots = legshots
        )
    }
}

data class RiotEconomy(
    val loadoutValue: Int,
    val weapon: String?,
    val armor: String?,
    val remaining: Int,
    val spent: Int
) {
    fun toCaupanharmEconomy(): RoundEconomy {
        return RoundEconomy(
            loadoutValue = loadoutValue,
            spent = spent,
            remaining = remaining,
            weapon = weapon?.let { Weapons.getNameFromUUID(it) },
            armor = armor?.let { Armors.getNameFromUUID(it) }
        )
    }
}

data class RiotRoundAbilityCasts(
    val grenadeEffects: Int?,
    val ability1Effects: Int?,
    val ability2Effects: Int?,
    val ultimateEffects: Int?
) {
    fun toCaupanharmAbilities(): CaupanharmAbilities {
        return CaupanharmAbilities(
            ability1 = ability1Effects,
            ability2 = ability2Effects,
            ability3 = grenadeEffects,
            ultimate = ultimateEffects
        )
    }
}

data class RiotRoundPlayerEconomy(
    val subject: String,
    val loadoutValue: Int,
    val weapon: String?,
    val armor: String?,
    val remaining: Int,
    val spent: Int
)

data class RiotRoundPlayerScore(
    val subject: String,
    val score: Int
)

data class RiotMatchKill(
    val gameTime: Int,
    val roundTime: Int,
    val round: Int,
    val killer: String?,
    val victim: String,
    val victimLocation: RiotLocation,
    val assistants: List<String>,
    val playerLocations: List<RiotPlayerLocation>,
    val finishingDamage: RiotFinishingDamage
) {
    fun toCaupanharmMatchKill(): CaupanharmMatchKill {
        return CaupanharmMatchKill(
            round = round,
            roundTimeMillis = roundTime,
            matchTimeMillis = gameTime,
            killer = killer,
            victim = victim,
            assistants = assistants,
            location = victimLocation.toCaupanharmLocation(),
            weapon = finishingDamage.toCaupanharmFinishingDamage(),
            playerLocations = playerLocations.map { it.toCaupanharmPlayerLocation() }
        )
    }
}

data class RiotFinishingDamage(
    val damageType: String,
    val damageItem: String,
    val isSecondaryFireMode: Boolean
) {
    fun toCaupanharmFinishingDamage(): CaupanharmFinishingDamage {
        return CaupanharmFinishingDamage(damageType, damageItem, isSecondaryFireMode)
    }
}