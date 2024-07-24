package perso.caupanharm.backend

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import org.springframework.stereotype.Repository
import perso.caupanharm.backend.models.localdata.AdditionalCustomPlayerData
import perso.caupanharm.backend.models.localdata.BracketMatchData
import perso.caupanharm.backend.models.localdata.PlayersMatchData
import perso.caupanharm.backend.models.TestData
import java.io.File

@Repository
class LocalDataService {
    val mapper = jacksonObjectMapper()
    private val logger = KotlinLogging.logger {}

    // Fonction générique pour éviter la redondance du code
    fun <T> getLocalJsonData(fileName: String, typeReference: TypeReference<List<T>>): List<T> {
        return try {
            val file = File("src/main/resources/data/${fileName}.json")
            mapper.readValue(file, typeReference)
        } catch (exception: Exception) {
            logger.error("Error reading JSON data from $fileName: ", exception)
            emptyList()
        }
    }

    // Création d'un TypeReference par type d'objet à déserialiser
    val testDataTypeReference = object : TypeReference<List<TestData>>() {}
    val bracketMatchDataTypeReference = object : TypeReference<List<BracketMatchData>>() {}
    val playersMatchesDataTypeReference = object : TypeReference<List<PlayersMatchData>>() {}
    val additionalCustomPlayerDataTypeReference = object : TypeReference<List<AdditionalCustomPlayerData>>() {}

    // Récupération des données désérialisées
    fun getTestData(): List<TestData> {
        return getLocalJsonData("testData", testDataTypeReference)
    }

    fun getBracketData(): List<BracketMatchData> {
        return getLocalJsonData("bracket", bracketMatchDataTypeReference)
    }

    fun getPlayersMatchesData(): List<PlayersMatchData> {
        return getLocalJsonData("players_matches", playersMatchesDataTypeReference)
    }

    fun getAdditionalPlayerData(): List<AdditionalCustomPlayerData> {
        return getLocalJsonData("additional_custom_players_data", additionalCustomPlayerDataTypeReference)
    }
}