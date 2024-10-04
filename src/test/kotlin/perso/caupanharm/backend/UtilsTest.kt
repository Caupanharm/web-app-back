package perso.caupanharm.backend

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import perso.caupanharm.backend.models.caupanharm.*
import perso.caupanharm.backend.models.henrik.*
import java.io.File

class UtilsTest {
// DEPRECATED
/*
val mapper = jacksonObjectMapper()
val logger = KotlinLogging.logger {}


@Test
fun testJsonToHenrikMatch() {
    val jsonFile = File("src/main/resources/testData/HenrikMatchV4.json")
    val typeref = object : TypeReference<HenrikMatchResponseV4>() {}

    val henrikMatchV4 = try {
        mapper.readValue(jsonFile, typeref).data
    } catch (exception: Exception) {
        null
    }

    // 1) Assert that the file got converted to an object
    assertNotNull(henrikMatchV4)

    val oracle = getHenrikMatchOracle()
    // 2) Assert that the file matches the oracle (destructured for readability)
    assertEquals(oracle.metadata, henrikMatchV4!!.metadata)
    assertEquals(oracle.players, henrikMatchV4.players)
    assertEquals(oracle.observers, henrikMatchV4.observers)
    assertEquals(oracle.coaches, henrikMatchV4.coaches)
    assertEquals(oracle.teams, henrikMatchV4.teams)
    assertEquals(oracle.rounds, henrikMatchV4.rounds)
    assertEquals(oracle.kills, henrikMatchV4.kills)
}

@Test
fun testJsonToCaupanharmMatch() {
    val typeref = object : TypeReference<HenrikMatchResponseV4>() {}
    val henrikMatchV4 = try {
        val file = File("src/main/resources/testData/HenrikMatchV4.json")
        mapper.readValue(file, typeref)
    } catch (exception: Exception) {
        logger.error("Error reading JSON data: ", exception)
        null
    }

    // 1) Assert that the file got converted to an object
    assertNotNull(henrikMatchV4)
    val caupanharmMatch = Utils.henrikToCaupanharmMatch(henrikMatchV4!!)
    assertNotNull(caupanharmMatch)

    val oracle = getCaupanharmMatchOracle()
    // 2) Assert that the file matches the oracle (destructured for readability)
    assertEquals(oracle.matchInfo, caupanharmMatch.matchInfo)
    assertEquals(oracle.players, caupanharmMatch.players)
    assertEquals(oracle.teams, caupanharmMatch.teams)
    assertEquals(oracle.rounds, caupanharmMatch.rounds)
    assertEquals(oracle.kills, caupanharmMatch.kills)
    logger.debug(mapper.writeValueAsString(caupanharmMatch))
}

private fun getHenrikMatchOracle(): HenrikMatchV4 {
    return HenrikMatchV4(
        HenrikMetadata(
            "b65f2ae7-578e-48f3-bd52-47235c6bc4b6",
            HenrikMap("2c9d57ec-4431-9c5e-2939-8f9ef6dd5cba", "Bind"),
            "release-09.02-shipping-20-2703179",
            1819068,
            "2024-08-13T11:47:44.336Z",
            true,
            HenrikQueue("competitive", "Competitive", "Standard"),
            HenrikSeason("52ca6698-41c1-e7de-4008-8994d2221209", "e9a1"),
            "pc",
            null,
            listOf(HenrikPenalty("3b99b8b3-6103-4526-abb2-9c21f3ca5413", 0)),
            "eu",
            "London"
        ),

        mutableListOf<HenrikPlayer>(
            HenrikPlayer(
                "b441ec66-669d-550d-bd34-24a678c5eb6f",
                "Player 1",
                "P1",
                "Red",
                "pc",
                "3b99b8b3-6103-4526-abb2-9c21f3ca5413",
                HenrikAgent("e370fa57-4757-3604-3648-499e1f642d3f", "Gekko"),
                HenrikStats(
                    4313, 15, 14, 4, 14, 27, 3, HenrikDamage(2977, 2596)
                ),
                HenrikAbilitiesPlayer(6, 10, 16, 4),
                HenrikTier(18, "Diamond 1"),
                HenrikCustomization(
                    "9ef2f845-4ab5-2350-ea38-0795bb21ed05",
                    "bc6a30d8-4d83-82aa-dfe9-7389c776f312",
                    "cbd1914e-43f8-7ae5-38c4-228bcbe58756"
                ),
                452,
                8100000,
                HenrikBehavior(0, HenrikFriendlyFire(0, 0), 0),
                HenrikEconomyShort(HenrikEconomyDetails(44600, 2477.7778), HenrikEconomyDetails(57850, 3213.889))
            )
        ),

        mutableListOf<HenrikObserver>(),
        mutableListOf<HenrikCoach>(),

        mutableListOf<HenrikTeam>(
            HenrikTeam("Red", HenrikRoundsShort(5, 13), false, null)
        ),

        mutableListOf<HenrikRound>(
            HenrikRound(
                0, "Defuse", "CeremonyDefault", "Blue", HenrikPlantEvents(
                    19416, "A", HenrikLocation(10446, 1732), HenrikPlayerShort(
                        "b441ec66-669d-550d-bd34-24a678c5eb6f", "Player 1", "P1", "Red"
                    ), listOf(
                        HenrikPlayerLocation(
                            HenrikPlayerShort(
                                "b441ec66-669d-550d-bd34-24a678c5eb6f", "Player 1", "P1", "Red"
                            ),
                            5.1327944,
                            HenrikLocation(9410, 2262),
                        )
                    )
                ), HenrikDefuseEvents(
                    39287, HenrikLocation(10498, 1756), HenrikPlayerShort(
                        "b441ec66-669d-550d-bd34-24a678c5eb6f", "Player 1", "P1", "Red"
                    ), listOf(
                        HenrikPlayerLocation(
                            HenrikPlayerShort(
                                "b441ec66-669d-550d-bd34-24a678c5eb6f", "Player 1", "P1", "Red"
                            ),
                            3.6148252,
                            HenrikLocation(10498, 1756),
                        )
                    )
                ), listOf(
                    HenrikPlayerStats(
                        HenrikPlayerShort(
                            "b441ec66-669d-550d-bd34-24a678c5eb6f", "Player 1", "P1", "Red"
                        ), HenrikAbilitiesRound(null, null, null, null),
                        listOf(
                            HenrikDamageEvents(
                                HenrikPlayerShort(
                                    "1c962e38-4bdf-5d2f-8f91-fd8d7f2116af", "Player 2", "P2", "Blue"
                                ), 105, 1, 0, 0
                            )
                        ), HenrikStats(193, 1, null, null, 1, 0, 0, null), HenrikEconomy(
                            800, 0, HenrikWeapon(
                                "1baa85b4-4c70-1284-64bb-6481dfc3bb4e", "Ghost", "Weapon"
                            ), null
                        ), was_afk = false, received_penalty = false, stayed_in_spawn = false
                    )
                )
            )
        ),

        mutableListOf(
            HenrikKillEvents(
                0, 11482, 66501, HenrikPlayerShort(
                    "b441ec66-669d-550d-bd34-24a678c5eb6f", "Player 1", "P1", "Red"
                ), HenrikPlayerShort(
                    "60c156ab-c597-5596-893c-acf0227a2682", "Player 2", "P2", "Blue"
                ), listOf(), HenrikLocation(10702, -48), HenrikWeapon(
                    "29a0cfab-485b-f5d5-779a-b59f85e204a8", "Classic", "Weapon"
                ), true, listOf(
                    HenrikPlayerLocation(
                        HenrikPlayerShort(
                            "b441ec66-669d-550d-bd34-24a678c5eb6f", "Player 1", "P1", "Red"
                        ), 0.9495989, HenrikLocation(9426, 592)
                    )
                )
            )
        ),
    )
}

private fun getCaupanharmMatchOracle(): CaupanharmMatch {
    return CaupanharmMatch(
        MatchInfo(
            "b65f2ae7-578e-48f3-bd52-47235c6bc4b6",
            "Bind",
            1819068,
            "2024-08-13T11:47:44.336Z",
            true,
            "Competitive",
            "e9a1"
        ),

        listOf(
            CaupanharmPlayer(
                "b441ec66-669d-550d-bd34-24a678c5eb6f",
                "Player 1#P1",
                "Red",
                "3b99b8b3-6103-4526-abb2-9c21f3ca5413",
                "Diamond 1",
                "Gekko",
                CaupanharmPlayerStats(
                    4313,
                    15,
                    14,
                    4,
                    14,
                    27,
                    3,
                    2977,
                    2596
                ),
                CaupanharmAbilities(10, 16, 6, 4),
                BehaviorSummary(0, 0, 0, 0),
                TotalEconomy(44600, 57850)
            )
        ),
        listOf(CaupanharmTeam("Red", 5, 13)),
        listOf(
            CaupanharmRound(
                "Blue",
                "Defuse",
                "CeremonyDefault",
                BombEvent(
                    19416,
                    "A",
                    Location(10446, 1732),
                    "Player 1#P1",
                    listOf(
                        PlayerLocation(
                            "Player 1#P1",
                            Location(9410, 2262),
                            5.1327944
                        )
                    )
                ),
                BombEvent(
                    39287,
                    null,
                    Location(10498, 1756),
                    "Player 1#P1",
                    listOf(
                        PlayerLocation(
                            "Player 1#P1",
                            Location(10498, 1756),
                            3.6148252
                        )
                    )
                ),
                listOf(
                    RoundPlayerStats(
                        "Player 1#P1",
                        CaupanharmAbilities(0, 0, 0, 0),
                        listOf(
                            DamageEvent(
                                "Player 2#P2",
                                105,
                                1,
                                0,
                                0
                            )
                        ),
                        RoundPlayerStatsRecap(
                            193,
                            1,
                            1,
                            0,
                            0
                        ),
                        RoundEconomy(
                            800,
                            0,
                            "Ghost",
                            null
                        ),
                        RoundBehavior(
                            false,
                            false,
                            false
                        )
                    )
                )
            )
        ),
        listOf(
            CaupanharmKill(
                0,
                11482,
                66501,
                "Player 1#P1",
                "Player 2#P2",
                listOf(),
                Location(10702, -48),
                "Classic",
                true,
                listOf(
                    PlayerLocation(
                        "Player 1#P1",
                        Location(9426, 592),
                        0.9495989
                    )
                )
            )
        )
    )
}
 */
}