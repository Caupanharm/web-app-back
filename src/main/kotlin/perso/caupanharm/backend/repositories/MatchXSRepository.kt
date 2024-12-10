package perso.caupanharm.backend.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import perso.caupanharm.backend.models.caupanharm.valorant.database.PostGresCompQuery
import perso.caupanharm.backend.models.caupanharm.valorant.database.PostGresMatchXS

interface MatchXSRepository: JpaRepository<PostGresMatchXS, Long> {

    @Query("""SELECT COUNT(m) FROM matches_xs m WHERE m.match_id = :uuid""", nativeQuery = true)
    fun countByMatchId(@Param("uuid") uuid: String): Int

    @Query("""SELECT m.* FROM matches_xs m WHERE m.match_id = :uuid""", nativeQuery = true)
    fun findByMatchId(@Param("uuid") uuid: String): PostGresMatchXS

    @Query("""SELECT m.* FROM matches_xs m WHERE m.rank = :rank""", nativeQuery = true)
    fun findByMatchRank(@Param("rank") rank: Int): List<PostGresMatchXS>

    @Query("""SELECT m.match_id FROM matches_xs m""", nativeQuery = true)
    fun findAllMatchIds(): List<String>

    @Query("""
        SELECT 
    CASE
        WHEN a.team = 'Blue' THEN m.red_score
        ELSE m.blue_score
    END AS enemy_score,    
    CASE
        WHEN a.team = 'Blue' THEN m.blue_score
        ELSE m.red_score
    END AS ally_score,    
    CASE 
        WHEN a.team = 'Blue' THEN m.blue_score_atk
        ELSE m.red_score_atk
    END AS attack_score,
    CASE 
        WHEN a.team = 'Blue' THEN m.blue_score_def
        ELSE m.red_score_def
    END AS defense_score,
    STRING_AGG(DISTINCT a_all.agent, ',' ORDER BY a_all.agent) AS team_agents
FROM matches_xs_agents a
JOIN matches_xs m ON a.match_id = m.match_id
JOIN matches_xs_agents a_all 
    ON a.match_id = a_all.match_id AND a.team = a_all.team
WHERE 
    (:agents IS NULL OR a.agent = ANY(string_to_array(:agents, ',')))
    AND (:map IS NULL OR m.map = :map)   
GROUP BY m.match_id, m.map, a.team
HAVING (:agents IS NULL OR COUNT(DISTINCT a.agent) = :agentCount)
    """, nativeQuery = true)
    fun findMatchesWithAgentsAndMap(
        @Param("map") map: String?,
        @Param("agents") agents: String?,
        @Param("agentCount") agentCount: Int = agents?.split(",")?.size ?: 0
    ): List<Map<String, Any>>


}