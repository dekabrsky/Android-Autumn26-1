package model

data class Team(
    val name: String,
    val city: String
) {
    val players = mutableListOf<Player>()

    val totalCost: Long by lazy {
        players.sumOf { it.marketValue }
    }

    val avgRedCards: Double by lazy {
        if (players.isEmpty()) 0.0 else players.map { it.redCards }.average()
    }
}