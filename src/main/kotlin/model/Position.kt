package model

enum class Position(val russianName: String) {
    MIDFIELD("полузащитник"),
    DEFENDER("защитник"),
    FORWARD("нападающий"),
    GOALKEEPER("вратарь");

    companion object {
        fun fromCsvTokenOrNull(token: String): Position? {
            return when (token.trim().uppercase()) {
                "MIDFIELD" -> MIDFIELD
                "DEFENDER" -> DEFENDER
                "FORWARD" -> FORWARD
                "GOALKEEPER" -> GOALKEEPER
                else -> null
            }
        }
    }
}
