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

}