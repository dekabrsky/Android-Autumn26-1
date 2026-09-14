package model

enum class Position(val titleRu: String) {
    FORWARD("нападающий"),
    MIDFIELD("полузащитник"),
    DEFENDER("защитник"),
    GOALKEEPER("вратарь");

    companion object {
        fun fromCsvTokenOrNull(token: String): Position? {
            val normalized = token.trim().lowercase()
            return when {
                normalized.contains("напад") || normalized.contains("forward")-> FORWARD
                normalized.contains("полузащ") || normalized.contains("midfield")-> MIDFIELD
                normalized.contains("защит") || normalized.contains("defender") -> DEFENDER
                normalized.contains("врат") || normalized.contains("goalkeeper")-> GOALKEEPER
                else -> entries.firstOrNull {
                    it.titleRu.equals(normalized, ignoreCase = true) || it.name.equals(normalized, ignoreCase = true)
                }
            }
        }
    }
}