package perso.caupanharm.backend

import org.springframework.boot.test.context.SpringBootTest
import perso.caupanharm.backend.models.caupanharm.valorant.match.*
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest
class RankComputingTest {
    private val placeholderMetadata = CaupanharmMatchMetadata("","",0,0,null,"")
    private val placeholderScore = CaupanharmMatchScore(0,0,0,0,0,0)
    private val placeholderStats = CaupanharmPlayerStats(0,0,0,0)
    private val placeholderAbilities = CaupanharmAbilities(0,0,0,0)
    private val placeholderBehavior = BehaviorSummary(0,0,0)

    @Test
    fun `average rank round down`(){
        val players: List<CaupanharmMatchPlayer> = listOf(
            CaupanharmMatchPlayer("","","","",20,"", placeholderStats, placeholderAbilities, placeholderBehavior),
            CaupanharmMatchPlayer("","","","",10,"", placeholderStats, placeholderAbilities, placeholderBehavior),
            CaupanharmMatchPlayer("","","","",10,"", placeholderStats, placeholderAbilities, placeholderBehavior),
            CaupanharmMatchPlayer("","","","",10,"", placeholderStats, placeholderAbilities, placeholderBehavior),
            CaupanharmMatchPlayer("","","","",10,"", placeholderStats, placeholderAbilities, placeholderBehavior),
            CaupanharmMatchPlayer("","","","",10,"", placeholderStats, placeholderAbilities, placeholderBehavior),
            CaupanharmMatchPlayer("","","","",10,"", placeholderStats, placeholderAbilities, placeholderBehavior),
            CaupanharmMatchPlayer("","","","",10,"", placeholderStats, placeholderAbilities, placeholderBehavior),
            CaupanharmMatchPlayer("","","","",10,"", placeholderStats, placeholderAbilities, placeholderBehavior),
            CaupanharmMatchPlayer("","","","",10,"", placeholderStats, placeholderAbilities, placeholderBehavior),
        )
        val caupanharmMatchFull = CaupanharmMatchFull(placeholderMetadata, players, placeholderScore, emptyList(), emptyList())
        val computedPostGresMatchXS = caupanharmMatchFull.toPostgresMatchXS()
        val expectedRank = 10
        assertEquals(expectedRank, computedPostGresMatchXS.rank)
    }

    @Test
    fun `average rank round up`(){

    }

    @Test
    fun `average rank with unranked players`(){

    }

    @Test
    fun `average rank round up with unranked players`(){

    }

    @Test
    fun `average rank round down with unranked players`(){

    }

}