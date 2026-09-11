package model

data class Team(
    val name: String,
    val city: String,
) {
    private val roster = mutableListOf<Player>()

    val players: List<Player>
        get() = roster

    fun addPlayer(player: Player) {
        roster += player
    }

    val totalTransferCost: Long by lazy { players.sumOf { it.transferCost.toLong() } }
    val averageRedCards: Double by lazy { players.map(Player::redCards).average() }
}
