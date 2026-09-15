package model

// Задание: добавьте константы позиций из CSV (FORWARD, MIDFIELD, DEFENDER, GOALKEEPER)
// с русским названием у каждой, а также companion-функцию
// fromCsvTokenOrNull(token: String): Position?, не зависящую от регистра и лишних пробелов.
enum class Position(val russianName: String) {
    GOALKEEPER("вратарь"),
    FORWARD("нападающий"),
    DEFENDER("защитник"),
    MIDFIELD("полузащитник");

    companion object {
        fun fromCsvTokenOrNull(token: String): Position? {
            val normalized = token.trim().uppercase()
            if (normalized.isEmpty()) return null
            return values().firstOrNull { it.name == normalized }
        }
    }
}
