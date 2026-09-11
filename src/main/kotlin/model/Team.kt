package model

data class Team(
    val name: String,
    val city: String,
) {
    var players: List<Player> = emptyList()
        private set

    constructor(name: String, city: String, players: List<Player>) : this(name, city) {
        this.players = players
    }

    val totalTransferCost: Long by lazy {
        players.sumOf { it.transferCost.toLong() }
    }

    val averageRedCards: Double by lazy {
        if (players.isEmpty()) 0.0 else players.map(Player::redCards).average()
    }
}
