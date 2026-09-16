package model

enum class Position(val russianName: String) {
    FORWARD("нападающий"),
    MIDFIELD("полузащитник"),
    DEFENDER("защитник"),
    GOALKEEPER("вратарь");

    companion object {
        fun fromCsvTokenOrNull(token: String): Position? {
            val cleanedToken = token.trim()

            return entries.firstOrNull { position ->
                position.name.equals(cleanedToken, ignoreCase = true)
            }
        }
    }
}
