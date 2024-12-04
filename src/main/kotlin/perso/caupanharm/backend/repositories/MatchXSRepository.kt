package perso.caupanharm.backend.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import perso.caupanharm.backend.models.caupanharm.valorant.database.PostGresMatchXS

interface MatchXSRepository: JpaRepository<PostGresMatchXS, Long> {

    @Query("""SELECT COUNT(m) FROM matches_xs m WHERE m.match_id = :uuid""", nativeQuery = true)
    fun countByMatchId(@Param("uuid") uuid: String): Int


}