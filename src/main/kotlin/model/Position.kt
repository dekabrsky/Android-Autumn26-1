package model

enum class Position(val ruName: String) {
    GOALKEEPER("вратарь"),
    DEFENDER("защитник"),
    MIDFIELDER("полузащитник"),
    FORWARD("нападающий");

    companion object {
        fun fromCsvTokenOrNull(token: String): Position? =
            when (token.trim().lowercase()) {
                "goalkeeper", "gk", "keeper", "вратарь" -> GOALKEEPER
                "defender", "def", "defence", "defense", "защитник" -> DEFENDER
                "midfielder", "midfield", "mid", "полузащитник" -> MIDFIELDER
                "forward", "striker", "attack", "attacker", "fw", "нападающий" -> FORWARD
                else -> null
            }
    }
}
