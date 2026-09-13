package filter

import model.Player
import model.Position

sealed interface PlayerFilter {
    data class ByPosition(val position: Position) : PlayerFilter
    data class CheaperThan(val cost: Long) : PlayerFilter
    data class And(val left: PlayerFilter, val right: PlayerFilter) : PlayerFilter
    data class Or(val left: PlayerFilter, val right: PlayerFilter) : PlayerFilter
    data class Not(val filter: PlayerFilter) : PlayerFilter
}

fun PlayerFilter.matches(player: Player): Boolean = when (this) {
    is PlayerFilter.ByPosition -> player.position == position
    is PlayerFilter.CheaperThan -> player.transferCost < cost
    is PlayerFilter.And -> left.matches(player) && right.matches(player)
    is PlayerFilter.Or -> left.matches(player) || right.matches(player)
    is PlayerFilter.Not -> !filter.matches(player)
}