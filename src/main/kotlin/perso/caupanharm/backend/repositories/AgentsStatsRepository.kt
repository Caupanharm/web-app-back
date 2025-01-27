package perso.caupanharm.backend.repositories

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import perso.caupanharm.backend.models.caupanharm.valorant.database.PostGresAgentsStats

interface AgentsStatsRepository : CrudRepository<PostGresAgentsStats, String> {

    @Query("""SELECT * FROM agents_stats""", nativeQuery = true)
    fun getData(): List<PostGresAgentsStats>
}