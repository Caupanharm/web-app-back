package perso.caupanharm.backend.models.riot.assets

enum class Armors(val uuid: String, val displayName: String) {
    HEAVY("822bcab2-40a2-324e-c137-e09195ad7692","Heavy Armor"),
    LIGHT("4dec83d5-4902-9ab3-bed6-a7a390761157","Light Armor"),
    REGEN("b1b9086d-41bd-a516-5d29-e3b34a6f1644","Regen Shield");

    companion object{
        fun getNameFromUUID(uuid: String): String{
            return entries.find{ it.uuid == uuid}?.displayName ?: "Armor not found"
        }
    }
}