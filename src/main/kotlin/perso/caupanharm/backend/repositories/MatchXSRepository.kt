package perso.caupanharm.backend.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import perso.caupanharm.backend.models.caupanharm.valorant.database.PostGresMatchXS

interface MatchXSRepository: JpaRepository<PostGresMatchXS, Int> {

    @Query("""SELECT COUNT(*) FROM matches_xs""", nativeQuery = true)
    fun getNumberOfMatches(): Int

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
        COUNT(*) AS games_played,
        CAST(SUM(blue_score_def) + SUM(red_score_def) AS FLOAT) / 
            (CAST(SUM(blue_score_def + red_score_atk) + SUM(red_score_def + blue_score_atk) AS FLOAT)) AS defense_winrate,
        CAST(SUM(blue_score_atk) + SUM(red_score_atk) AS FLOAT) / 
            (CAST(SUM(blue_score_atk + red_score_atk) + SUM(blue_score_def + red_score_def) AS FLOAT)) AS attack_winrate
    FROM matches_xs m
    WHERE (:maps IS NULL OR m.map IN :maps)
""", nativeQuery = true)
    fun getMapRates(@Param("maps") maps: List<String>?): Map<String, Any>

}