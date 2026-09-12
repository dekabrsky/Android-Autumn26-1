package model

data class Team(
    val name: String,
    val city: String
) {
    private val mutablePlayers = mutableListOf<Player>()

    val players: List<Player>
        get() = mutablePlayers

    fun registerPlayer(player: Player) {
        mutablePlayers.add(player)
    }

    val totalTransferCost: Long by lazy {
        mutablePlayers.sumOf { it.transferCost }
    }

    val averageRedCards: Double by lazy {
        if (mutablePlayers.isEmpty()) 0.0
        else mutablePlayers.sumOf { it.redCards }.toDouble() / mutablePlayers.size
    }

    override fun toString(): String = "$name ($city)"
}
