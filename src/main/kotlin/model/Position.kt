package model

enum class Position(val russianName: String) {
    FORWARD("нападающий"),
    MIDFIELDER("полузащитник"),
    DEFENDER("защитник"),
    GOALKEEPER("вратарь");

    companion object {
        fun fromCsvTokenOrNull(token: String): Position? {
            val normalized = token.trim().lowercase()
            return when (normalized) {
                "forward", "нападающий" -> FORWARD
                "midfield", "midfielder", "полузащитник" -> MIDFIELDER
                "defender", "защитник" -> DEFENDER
                "goalkeeper", "вратарь" -> GOALKEEPER
                else -> null
            }
        }
    }
}