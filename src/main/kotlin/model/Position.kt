package model

// Задание: добавьте константы позиций из CSV (FORWARD, MIDFIELD, DEFENDER, GOALKEEPER)
// с русским названием у каждой, а также companion-функцию
// fromCsvTokenOrNull(token: String): Position?, не зависящую от регистра и лишних пробелов.Ы

enum class Position(val positionName: String) {
    FORWARD(positionName = "нападающий"),
    MIDFIELD(positionName = "полузащитник"),
    DEFENDER(positionName = "защитник"),
    GOALKEEPER(positionName = "вратарь");

    companion object {
        fun fromCsvTokenOrNull(token: String): Position? =
            values().firstOrNull {
                it.name.equals(other = token.trim(), ignoreCase = true)
            }
    }
}