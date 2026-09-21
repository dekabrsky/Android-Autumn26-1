package model

data class Team(val name: String, val city: String) {
    private val roster = mutableListOf<Player>()

    val players: List<Player>
        get() = roster

    val totalTransferCost: Long by lazy {
        roster.sumOf { it.transferCost.toLong() }
    }

    val averageRedCards: Double by lazy {
        roster.map { it.redCardsCount }.average().takeUnless { it.isNaN() } ?: 0.0
    }

    internal fun register(player: Player) {
        roster += player
    }
}
