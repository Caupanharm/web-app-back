package perso.caupanharm.backend.models.riot.assets

enum class Ceremonies(val uuid: String,val displayName: String, val  customDisplayName: String?, val path: String) {
    CLOSER(
        "b41f4d69-4f9d-ffa9-2be8-e2878cf7f03b",
        "CLOSER",
        null,
        "ShooterGame/Content/Ceremonies/CloserCeremony_PrimaryAsset"
    ),
    ACE(
        "1e71c55c-476e-24ac-0687-e48b547dbb35",
        "ACE",
        "Ace",
        "ShooterGame/Content/Ceremonies/AceCeremony_PrimaryAsset"
    ),
    CLUTCH(
        "a6100421-4ecb-bd55-7c23-e4899643f230",
        "CLUTCH",
        "Clutch",
        "ShooterGame/Content/Ceremonies/ClutchCeremony_PrimaryAsset"
    ),
    FLAWLESS(
        "eb651c62-421f-98fc-8008-68bee9ec942d",
        "FLAWLESS",
        "Flawless",
        "ShooterGame/Content/Ceremonies/FlawlessCeremony_PrimaryAsset"
    ),
    TEAM_ACE(
        "87c91747-4de4-635e-a64b-6ba4faeeae78",
        "TEAM ACE",
        "Team Ace",
        "ShooterGame/Content/Ceremonies/TeamAceCeremony_PrimaryAsset"
    ),
    THRIFTY(
        "bf94f35e-4794-8add-dc7d-fb90a08d3d04",
        "THRIFTY",
        "Thrifty",
        "ShooterGame/Content/Ceremonies/ThriftyCeremony_PrimaryAsset"
    );

    companion object {
        fun getCustomNameFromDisplayName(name: String): String {
            return entries.find { it.displayName == name }?.customDisplayName ?: "Ceremony not found"
        }
    }
}