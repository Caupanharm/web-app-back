package perso.caupanharm.backend.repositories

import org.springframework.data.jpa.repository.JpaRepository
import perso.caupanharm.backend.models.caupanharm.valorant.database.PostGresMatchXSPlayer

interface MatchXSAgentRepository: JpaRepository<PostGresMatchXSPlayer, Long> {

}