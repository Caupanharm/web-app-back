package perso.caupanharm.backend.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import perso.caupanharm.backend.models.caupanharm.valorant.database.PostGresMatchXSPlayer

interface MatchXSAgentRepository: JpaRepository<PostGresMatchXSPlayer, Long> {

    @Query("""SELECT m.player_id FROM matches_xs_agents m WHERE m.match_id = :uuid""", nativeQuery = true)
    fun findByMatchId(@Param("uuid") uuid: String): List<String>

}