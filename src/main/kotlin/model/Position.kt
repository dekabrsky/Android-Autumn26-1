package model

// Задание: добавьте константы позиций из CSV (FORWARD, MIDFIELD, DEFENDER, GOALKEEPER)
// с русским названием у каждой, а также companion-функцию
// fromCsvTokenOrNull(token: String): Position?, не зависящую от регистра и лишних пробелов.
enum class Position(
    val className: String
) {
    FORWARD("нападающий"),
    MIDFIELD("полузащитник"),
    DEFENDER("защитник"),
    GOALKEEPER("вратарь");

    companion object {
        fun fromCsvTokenOrNull(token: String): Position? =
            when (token.trim().uppercase()) {
                "FORWARD" -> FORWARD
                "MIDFIELD" -> MIDFIELD
                "DEFENDER" -> DEFENDER
                "GOALKEEPER" -> GOALKEEPER
                else -> null
            }
    }
}
