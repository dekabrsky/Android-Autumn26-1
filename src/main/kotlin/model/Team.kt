package model

data class Team(
    val name: String,
    val city: String,
) {
    private val playersList = mutableListOf<Player>()

    val players: List<Player>
        get() = playersList

    fun addPlayer(player: Player) {
        playersList += player
    }

    val totalTransferCost: Long by lazy { players.sumOf { it.transferCost } }
    val averageRedCards: Double by lazy {
        if (players.isEmpty()) 0.0 else players.sumOf { it.redCards }.toDouble() / players.size
    }
}