package model

// Задание: добавьте константы позиций из CSV (FORWARD, MIDFIELD, DEFENDER, GOALKEEPER)
// с русским названием у каждой, а также companion-функцию
// fromCsvTokenOrNull(token: String): Position?, не зависящую от регистра и лишних пробелов.
enum class Position(val displayName: String) {
    DEFENDER("защитник"),
    FORWARD("нападающий"),
    GOALKEEPER("вратарь"),
    MIDFIELD("полузащитник");

    companion object {
        fun fromCsvTokenOrNull(token: String): Position? {
            val cleanedToken = token.trim().uppercase()

            return entries.find { it.name == cleanedToken }
        }
    }
}
