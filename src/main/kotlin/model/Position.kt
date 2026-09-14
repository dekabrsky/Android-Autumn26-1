package model

enum class Position(val russianName: String) {
    FORWARD("нападающий"),
    MIDFIELD("полузащитник"),
    DEFENDER("защитник"),
    GOALKEEPER("вратарь");

    companion object {
        // Регистр и пробелы по краям не важны: " defender " -> DEFENDER.
        fun fromCsvTokenOrNull(token: String): Position? {
            val normalized = token.trim()
            return entries.firstOrNull { it.name.equals(normalized, ignoreCase = true) }
        }
    }
}
