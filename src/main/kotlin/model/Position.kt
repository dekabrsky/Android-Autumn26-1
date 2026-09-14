package model

// Задание: добавьте константы позиций из CSV (FORWARD, MIDFIELD, DEFENDER, GOALKEEPER)
// с русским названием у каждой, а также companion-функцию
// fromCsvTokenOrNull(token: String): Position?, не зависящую от регистра и лишних пробелов.
enum class Position(val russianName: String) {
    FORWARD("нападающий"),
    MIDFIELDER("полузащитник"),
    DEFENDER("защитник"),
    GOALKEEPER("вратарь");

    companion object {
        fun fromCsvTokenOrNull(token: String): Position? {
            val normalized = token.trim().lowercase()
            return entries.find {
                it.russianName == normalized ||
                        it.name.lowercase() == normalized
            }
        }
    }
}
