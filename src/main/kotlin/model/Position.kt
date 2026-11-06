package model

enum class Position(val russianName: String) {
    FORWARD("нападающий"),
    MIDFIELD("полузащитник"),
    DEFENDER("защитник"),
    GOALKEEPER("вратарь");

    companion object {
        /** Не зависит от регистра и лишних пробелов; для неизвестного токена возвращает null. */
        fun fromCsvTokenOrNull(token: String): Position? {
            val normalized = token.trim().uppercase()
            return entries.firstOrNull { it.name == normalized }
        }
    }
}
