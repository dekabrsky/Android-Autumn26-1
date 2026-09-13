package filter

import model.Player
import model.Position

sealed interface PlayerFilter {
    fun matches(player: Player): Boolean = when (this) {
        is ByPosition -> player.position == position
        is CheaperThan -> player.transferCost < maxCost
        is And -> left.matches(player) && right.matches(player)
        is Or -> left.matches(player) || right.matches(player)
        is Not -> !inner.matches(player)
    }

    data class ByPosition(val position: Position) : PlayerFilter
    data class CheaperThan(val maxCost: Long) : PlayerFilter
    data class And(val left: PlayerFilter, val right: PlayerFilter) : PlayerFilter
    data class Or(val left: PlayerFilter, val right: PlayerFilter) : PlayerFilter
    data class Not(val inner: PlayerFilter) : PlayerFilter
}

fun List<Player>.filterBy(playerFilter: PlayerFilter): List<Player> =
    filter { playerFilter.matches(it) }
