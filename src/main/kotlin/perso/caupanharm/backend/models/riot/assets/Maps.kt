package perso.caupanharm.backend.models.riot.assets

enum class Maps(val uuid: String, val displayName: String, val mapUrl: String, val tacticalDescription: String?) {
    ASCENT("7eaecc1b-4337-bbf6-6ab9-04b8f06b3319", "Ascent", "/Game/Maps/Ascent/Ascent", "A/B Sites"),
    SPLIT("d960549e-485c-e861-8d71-aa9d1aed12a2", "Split", "/Game/Maps/Bonsai/Bonsai","A/B Sites"),
    FRACTURE("b529448b-4d60-346e-e89e-00a4c527a405", "Fracture", "/Game/Maps/Canyon/Canyon","A/B Sites"),
    BIND("2c9d57ec-4431-9c5e-2939-8f9ef6dd5cba", "Bind", "/Game/Maps/Duality/Duality","A/B Sites"),
    BREEZE("2fb9a4fd-47b8-4e7d-a969-74b4046ebd53", "Breeze","/Game/Maps/Foxtrot/Foxtrot","A/B Sites"),
    ABYSS("224b0a95-48b9-f703-1bd8-67aca101a61f","Abyss","/Game/Maps/Infinity/Infinity","A/B Sites"),
    LOTUS("2fe4ed3a-450a-948b-6d6b-e89a78e680a9", "Lotus", "/Game/Maps/Jam/Jam", "A/B/C Sites"),
    SUNSET("92584fbe-486a-b1b2-9faa-39b0f486b498", "Sunset", "/Game/Maps/Juliett/Juliett", "A/B Sites"),
    PEARL("fd267378-4d1d-484f-ff52-77821ed10dc2", "Pearl","/Game/Maps/Pitt/Pitt", "A/B Sites"),
    ICEBOX("e2ad5c54-4114-a870-9641-8ea21279579a", "Icebox","/Game/Maps/Port/Port","A/B Sites"),
    HAVEN("2bee0dc9-4ffe-519b-1cbd-7fbe763a6047", "Haven", "/Game/Maps/Triad/Triad", "A/B/C Sites"),

    DISCTRICT("690b3ed2-4dff-945b-8223-6da834e30d24", "District","/Game/Maps/HURM/HURM_Alley/HURM_Alley", null),
    KASBAH("12452a9d-48c3-0b02-e7eb-0381c3520404", "Kasbah", "/Game/Maps/HURM/HURM_Bowl/HURM_Bowl", null),
    DRIFT("2c09d728-42d5-30d8-43dc-96a05cc7ee9d", "Drift","/Game/Maps/HURM/HURM_Helix/HURM_Helix", null),
    GLITCH("d6336a5a-428f-c591-98db-c8a291159134", "Glitch","/Game/Maps/HURM/HURM_HighTide/HURM_HighTide", null),
    PIAZZA("de28aa9b-4cbe-1003-320e-6cb3ec309557", "Piazza", "/Game/Maps/HURM/HURM_Yard/HURM_Yard", null),

    TRAINING("1f10dab3-4294-3827-fa35-c2aa00213cf3", "Basic Training", "/Game/Maps/NPEV2/NPEV2", null),
    RANGE("ee613ee9-28b7-4beb-9666-08db13bb2244", "The Range", "/Game/Maps/Poveglia/Range", null),
    RANGE2("/Game/Maps/Poveglia/Range","The Range", "/Game/Maps/PovegliaV2/RangeV2", null);

    companion object{
        fun getNameFromUuid(mapUrl: String): String{
            return entries.find{ it.mapUrl == mapUrl}?.displayName?: "Map Not Found"
        }
    }
}