package perso.caupanharm.backend.models.riot.assets

enum class Ceremonies(val uuid: String, val customName: String, val  displayName: String?, val path: String) {
    DEFAULT(
        "",
        "CeremonyDefault",
        null,
        ""
    ),
    CLOSER(
        "b41f4d69-4f9d-ffa9-2be8-e2878cf7f03b",
        "CeremonyCloser",
        null,
        "ShooterGame/Content/Ceremonies/CloserCeremony_PrimaryAsset"
    ),
    ACE(
        "1e71c55c-476e-24ac-0687-e48b547dbb35",
        "CeremonyAce",
        "Ace",
        "ShooterGame/Content/Ceremonies/AceCeremony_PrimaryAsset"
    ),
    CLUTCH(
        "a6100421-4ecb-bd55-7c23-e4899643f230",
        "CeremonyClutch",
        "Clutch",
        "ShooterGame/Content/Ceremonies/ClutchCeremony_PrimaryAsset"
    ),
    FLAWLESS(
        "eb651c62-421f-98fc-8008-68bee9ec942d",
        "CeremonyFlawless",
        "Flawless",
        "ShooterGame/Content/Ceremonies/FlawlessCeremony_PrimaryAsset"
    ),
    TEAM_ACE(
        "87c91747-4de4-635e-a64b-6ba4faeeae78",
        "CeremonyTeamAce",
        "Team Ace",
        "ShooterGame/Content/Ceremonies/TeamAceCeremony_PrimaryAsset"
    ),
    THRIFTY(
        "bf94f35e-4794-8add-dc7d-fb90a08d3d04",
        "CeremonyThrifty",
        "Thrifty",
        "ShooterGame/Content/Ceremonies/ThriftyCeremony_PrimaryAsset"
    );

    companion object {
        fun getFromName(name: String): Ceremonies {
            return entries.find { it.customName == name }?: DEFAULT
        }
    }
}