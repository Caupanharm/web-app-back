package perso.caupanharm.backend.repositories

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import perso.caupanharm.backend.models.caupanharm.valorant.database.PostGresCompsStats

interface CompsStatsRepository : CrudRepository<PostGresCompsStats, String> {
    @Query("""SELECT * FROM comps_stats""", nativeQuery = true)
    fun getData(): List<PostGresCompsStats>
}