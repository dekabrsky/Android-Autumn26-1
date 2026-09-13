package model

data class Team(
    val name: String,
    val city: String,
    var numberOfPlayers: Int = 0
) {
    val players: MutableList<Player> = mutableListOf()

    val totalTransferCost by lazy {
        players.sumOf { it.transferCost }
    }

    val averageCards by lazy {
        players.sumOf { it.redCards + it.yellowCards } / players.size.toDouble()
    }
}