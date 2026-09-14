package model

data class Team(
    val name: String,
    val city: String,
    val players: List<Player> = emptyList()
) {
    val totalSquadValue: Long by lazy {
        players.sumOf { it.transferValue }
    }

    val totalCards: Int by lazy {
        players.sumOf { it.yellowCards + it.redCards }
    }

    val averageCards: Double by lazy {
        if (players.isEmpty()) 0.0
        else totalCards.toDouble() / players.size
    }

    val averageRedCards: Double by lazy {
        if (players.isEmpty()) 0.0
        else players.sumOf { it.redCards }.toDouble() / players.size
    }

    val displayName: String
        get() = "$name ($city)"
}