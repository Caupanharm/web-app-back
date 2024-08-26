package perso.caupanharm.backend.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import perso.caupanharm.backend.models.caupanharm.PostGresMatch

@Repository
interface MatchRepository : CrudRepository<PostGresMatch, String> {

}