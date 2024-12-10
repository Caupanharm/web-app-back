package perso.caupanharm.backend.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import perso.caupanharm.backend.models.caupanharm.valorant.database.PostGresMatchXSPlayer

interface MatchXSAgentRepository: JpaRepository<PostGresMatchXSPlayer, Long> {

    @Query("""SELECT m.player_id FROM matches_xs_agents m WHERE m.match_id = :uuid""", nativeQuery = true)
    fun findPlayerIdByMatchId(@Param("uuid") matchId: String): List<String>

    @Query("""SELECT m.* FROM matches_xs_agents m WHERE m.match_id = :uuid""", nativeQuery = true)
    fun findPlayersByMatchId(@Param("uuid") matchId: String): List<PostGresMatchXSPlayer>


    @Query("""
    SELECT
        COUNT(*) AS total_matches,
        (COUNT(CASE 
        WHEN (a.team = 'Blue' AND m.blue_score > m.red_score) OR 
             (a.team = 'Red' AND m.red_score > m.blue_score) 
        THEN 1 
        END)::FLOAT / COUNT(*)) * 100 AS win_rate,
        CAST(
        SUM(
            CASE 
                WHEN a.team = 'Blue' THEN m.blue_score_atk
                ELSE m.red_score_atk
            END
        ) AS FLOAT
        ) / 
        CAST(
        SUM(
            CASE 
                WHEN a.team = 'Blue' THEN m.blue_score_atk + m.red_score_def
                ELSE m.red_score_atk + m.blue_score_def
            END
        ) AS FLOAT
        ) * 100 AS attack_win_rate,
        CAST(
        SUM(
            CASE 
                WHEN a.team = 'Blue' THEN m.blue_score_def
                ELSE m.red_score_def
            END
        ) AS FLOAT
        ) / 
        CAST(
        SUM(
            CASE 
                WHEN a.team = 'Blue' THEN m.blue_score_def + m.red_score_atk
                ELSE m.red_score_def + m.blue_score_atk
            END
        ) AS FLOAT
        ) * 100 AS defense_win_rate
    FROM matches_xs_agents a
    INNER JOIN matches_xs m ON a.match_id = m.match_id
    WHERE a.agent = :agent AND (:map IS NULL OR m.map = :map)
    GROUP BY a.agent;
    """, nativeQuery = true)
    fun getMapAgentWinrate(@Param("map") map: String?, @Param("agent") agent: String): Map<String,Any>


    @Query("""
        SELECT
            m.map,
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