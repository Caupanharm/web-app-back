package perso.caupanharm.backend

import perso.caupanharm.backend.models.caupanharm.*
import perso.caupanharm.backend.models.valorant.match.full.CaupanharmMatchFull
import perso.caupanharm.backend.models.valorant.match.full.CaupanharmMatchMetadata


public class Utils {

    companion object {
        // TODO move these functions to the corresponding data classes
        fun caupanharmToPostgresMatch(match: CaupanharmMatchFull): PostGresMatch {
            return PostGresMatch(
                match.metadata.id,
                match.metadata.map,
                match.metadata.gameLengthMillis,
                match.metadata.gameStart,
                match.metadata.queue,
                match.metadata.season,
                match.players,
                match.teams,
                match.rounds,
                match.kills
            )
        }

        fun postgresToCaupanharmMatch(match: PostGresMatch): CaupanharmMatchFull {
            return CaupanharmMatchFull(
                CaupanharmMatchMetadata(
                    match.matchId,
                    match.map,
                    match.gameLengthMillis,
                    match.gameStart,
                    true,
                    match.queue,
                    match.season
                ),
                match.players,
                match.teams,
                match.rounds,
                match.kills
            )
        }
    }
}
