package model

enum class Position(val russianName: String) {
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
