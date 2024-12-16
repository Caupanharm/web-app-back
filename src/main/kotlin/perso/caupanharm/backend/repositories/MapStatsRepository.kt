package perso.caupanharm.backend.repositories

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import perso.caupanharm.backend.models.caupanharm.valorant.database.PostGresMapAgentsStats

interface MapStatsRepository : CrudRepository<PostGresMapAgentsStats, String> {

    @Query("""SELECT * FROM maps_stats""", nativeQuery = true)
    fun getData(): List<PostGresMapAgentsStats>
}