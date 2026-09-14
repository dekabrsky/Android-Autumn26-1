package model

/**
 * Позиция игрока на поле. Константы соответствуют токенам из CSV,
 * у каждой — русское название для вывода.
 */
enum class Position(val russianName: String) {
    FORWARD("нападающий"),
    MIDFIELD("полузащитник"),
    DEFENDER("защитник"),
    GOALKEEPER("вратарь");

    companion object {
        /**
         * Распознаёт позицию по токену из CSV без учёта регистра и лишних пробелов.
         * Возвращает `null`, если токен пустой или не соответствует ни одной позиции.
         */
        fun fromCsvTokenOrNull(token: String): Position? {
            val normalized = token.trim().uppercase()
            if (normalized.isEmpty()) return null
            return entries.firstOrNull { it.name == normalized }
        }
    }
}
