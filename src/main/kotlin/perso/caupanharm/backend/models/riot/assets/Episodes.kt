package perso.caupanharm.backend.models.riot.assets

import java.time.Instant

enum class Episodes(val uuid: String, val displayName: String, val title: String?, val type: String?, val startTime: Instant, val endTime: Instant, val parentUuid: String?) {

    E9(
        uuid = "d1ad9e7a-4e3f-e8c6-eb1b-148162a5acf7",
        displayName = "EPISODE 9",
        title = "COLLISION",
        type = null,
        startTime = Instant.parse("2024-06-26T00:00:00Z"),
        endTime = Instant.parse("2025-01-08T00:00:00Z"),
        parentUuid = null
    ),
    E9A1(
        uuid = "52ca6698-41c1-e7de-4008-8994d2221209",
        displayName = "ACT 1",
        title = "EPISODE 9 // ACT I",
        type = "EAresSeasonType::Act",
        startTime = Instant.parse("2024-06-26T00:00:00Z"),
        endTime = Instant.parse("2024-08-28T00:00:00Z"),
        parentUuid = "d1ad9e7a-4e3f-e8c6-eb1b-148162a5acf7",
    ),
    E9A2(
        uuid = "292f58db-4c17-89a7-b1c0-ba988f0e9d98",
        displayName = "ACT 2",
        title = "EPISODE 9 // ACT II",
        type = "EAresSeasonType::Act",
        startTime = Instant.parse("2024-08-28T00:00:00Z"),
        endTime = Instant.parse("2024-10-23T00:00:00Z"),
        parentUuid = "d1ad9e7a-4e3f-e8c6-eb1b-148162a5acf7"
    ),
    E9A3(
        uuid = "dcde7346-4085-de4f-c463-2489ed47983b",
        displayName = "ACT 3",
        title = "EPISODE 9 // ACT III",
        type = "EAresSeasonType::Act",
        startTime = Instant.parse("2024-10-23T00:00:00Z"),
        endTime = Instant.parse("2025-01-08T00:00:00Z"),
        parentUuid = "d1ad9e7a-4e3f-e8c6-eb1b-148162a5acf7"
    );

    companion object {
        fun getTitleFromUuid(uuid: String): String {
            return entries.find { it.uuid == uuid }?.title?: "Act not found"
        }
    }
}