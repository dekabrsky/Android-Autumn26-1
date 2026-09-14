package model

enum class Position(val russianName: String) {
    GOALKEEPER("вратарь"),
    DEFENDER("защитник"),
    MIDFIELD("полузащитник"),
    FORWARD("нападающий");

    companion object {
        /**
         * Возвращает Position или null, если токен не распознан.
         * Не зависит от регистра и лишних пробелов.
         */
        fun fromCsvTokenOrNull(token: String): Position? {
            val normalized = token.trim().uppercase()
            if (normalized.isEmpty()) return null
            return entries.firstOrNull { it.name == normalized }
        }
    }
}