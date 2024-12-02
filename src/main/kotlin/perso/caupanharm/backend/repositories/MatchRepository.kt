package perso.caupanharm.backend.repositories

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import perso.caupanharm.backend.models.caupanharm.valorant.database.PostGresMatch

@Repository
interface MatchRepository : CrudRepository<PostGresMatch, String> {

    // Recherche des matchs dont la colonne "players" contient un joueur avec un nom spécifique
    @Query("""
        SELECT m.*
        FROM matches m
        WHERE EXISTS (
            SELECT 1
            FROM jsonb_array_elements(m.players) AS player
            WHERE player->>'name' ILIKE :playerName
            ORDER BY m.game_start DESC
        )
    """, nativeQuery = true)
    fun findByPlayerName(playerName: String): List<PostGresMatch>


    @Query("""
    SELECT 
        m.players AS players,
        m.score AS score
    FROM matches m
    WHERE EXISTS (
        SELECT 1
        FROM jsonb_array_elements(m.players) AS player
        WHERE player->>'name' ILIKE :playerName
    )
""", nativeQuery = true)
    fun findTeamsByPlayerName(playerName: String): List<Map<String, Any>>

    @Query("""SELECT m.match_id FROM matches m""", nativeQuery = true)
    fun findMatchIds(): Set<String>

}