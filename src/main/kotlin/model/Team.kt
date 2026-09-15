package model

data class Team(val name: String, val city: String,
    val players: List<Player> = emptyList()) {
    val totalCost: Long by lazy {
        players.sumOf { it.transferCost.toLong() }
    }

    val averageRedCards: Double by lazy {
        if (players.isEmpty()) 0.0 else players.sumOf { it.redCards }.toDouble() / players.size
    }
}