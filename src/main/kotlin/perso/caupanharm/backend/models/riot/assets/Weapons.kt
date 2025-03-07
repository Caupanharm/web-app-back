package perso.caupanharm.backend.models.riot.assets

enum class Weapons(val uuid: String, val displayName: String, val category: String?) {
    MELEE("2f59173c-4bed-b6c3-2191-dea9b58be9c7", "Melee", "EEquippableCategory::Melee"),
    CLASSIC("29a0cfab-485b-f5d5-779a-b59f85e204a8","Classic","EEquippableCategory::Sidearm"),
    SHORTY("42da8ccc-40d5-affc-beec-15aa47b42eda", "Shorty", "EEquippableCategory::Sidearm"),
    FRENZY("44d4e95c-4157-0037-81b2-17841bf2e8e3","Frenzy","EEquippableCategory::Sidearm"),
    GHOST("1baa85b4-4c70-1284-64bb-6481dfc3bb4e", "Ghost","EEquippableCategory::Sidearm"),
    SHERIFF("e336c6b8-418d-9340-d77f-7a9e4cfe0702", "Sheriff", "EEquippableCategory::Sidearm"),
    BUCKY("910be174-449b-c412-ab22-d0873436b21b", "Bucky","EEquippableCategory::Shotgun"),
    JUDGE("ec845bf4-4f79-ddda-a3da-0db3774b2794","Judge","EEquippableCategory::Shotgun"),
    STINGER("f7e1b454-4ad4-1063-ec0a-159e56b58941", "Stinger", "EEquippableCategory::SMG"),
    SPECTRE("462080d1-4035-2937-7c09-27aa2a5c27a7", "Spectre", "EEquippableCategory::SMG"),
    ARES("55d8a0f4-4274-ca67-fe2c-06ab45efdf58","Ares","EEquippableCategory::Heavy"),
    ODIN("63e6c2b6-4a8e-869c-3d4c-e38355226584","Odin","EEquippableCategory::Heavy"),
    BULLDOG("ae3de142-4d85-2547-dd26-4e90bed35cf7","Bulldog","EEquippableCategory::Rifle"),
    PHANTOM("ee8e8d15-496b-07ac-e5f6-8fae5d4c7b1a","Phantom","EEquippableCategory::Rifle"),
    VANDAL("9c82e19d-4575-0200-1a81-3eacf00cf872","Vandal","EEquippableCategory::Rifle"),
    GUARDIAN("4ade7faa-4cf1-8376-95ef-39884480959b", "Guardian","EEquippableCategory::Rifle"),
    MARSHAL("c4883e50-4494-202c-3ec3-6b8a9284f00b", "Marshal", "EEquippableCategory::Sniper"),
    OUTLAW("5f0aaf7a-4289-3998-d5ff-eb9a5cf7ef5c","Outlaw","EEquippableCategory::Sniper"),
    OPERATOR("a03b24d3-4319-996d-0f8c-94bbfba1dfc7", "Operator","EEquippableCategory::Sniper");

    companion object{
        fun getNameFromUUID(uuid: String): String{
            return entries.find{it.uuid == uuid}?.displayName ?: "Weapon not found"
        }
    }
}