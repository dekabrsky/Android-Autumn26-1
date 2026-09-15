package model

enum class Position(val russianName: String) {
    FORWARD("нападающий"),
    MIDFIELD("полузащитник"),
    DEFENDER("защитник"),
    GOALKEEPER("вратарь");

    companion object {
        fun fromCsvTokenOrNull(token: String): Position? {
            val normalized = token.trim()

            if (normalized.isEmpty()) {
                return null
            }

            return entries.firstOrNull {
                it.name.equals(normalized, ignoreCase = true)
            }
        }
    }
}