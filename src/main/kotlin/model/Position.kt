package model

// Задание: добавьте константы позиций из CSV (FORWARD, MIDFIELD, DEFENDER, GOALKEEPER)
// с русским названием у каждой, а также companion-функцию
// fromCsvTokenOrNull(token: String): Position?, не зависящую от регистра и лишних пробелов.
enum class Position(val russianName: String) {
    FORWARD("нападающий"),
    MIDFIELD("полузащитник"),
    DEFENDER("защитник"),
    GOALKEEPER("вратарь");

    companion object {
        private val aliases = mapOf(
            "midfield" to MIDFIELD,
            "mid" to MIDFIELD,
            "striker" to FORWARD,
            "attack" to FORWARD,
            "attacker" to FORWARD,
            "fw" to FORWARD,
            "def" to DEFENDER,
            "defence" to DEFENDER,
            "defense" to DEFENDER,
            "gk" to GOALKEEPER,
            "keeper" to GOALKEEPER
        )

        fun fromCsvTokenOrNull(token: String): Position? {
            val normalized = token.trim().lowercase()
            entries.find {
                it.russianName == normalized || it.name.lowercase() == normalized
            }?.let { return it }
            return aliases[normalized]
        }
    }
}
