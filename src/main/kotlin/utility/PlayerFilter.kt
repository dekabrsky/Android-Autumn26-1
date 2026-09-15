package utility

import model.Player
import model.Position

sealed interface PlayerFilter {
    fun matches(player: Player): Boolean = evaluateFilter(this, player)

    data class ByPosition(val position: Position) : PlayerFilter
    data class CheaperThan(val budget: Int) : PlayerFilter
    data class And(val left: PlayerFilter, val right: PlayerFilter) : PlayerFilter
    data class Or(val left: PlayerFilter, val right: PlayerFilter) : PlayerFilter
    data class Not(val filter: PlayerFilter) : PlayerFilter
}

fun evaluateFilter(filter: PlayerFilter, player: Player): Boolean {
    return when (filter) {
        is PlayerFilter.ByPosition -> player.position == filter.position
        is PlayerFilter.CheaperThan -> player.transferCost < filter.budget
        is PlayerFilter.And -> evaluateFilter(filter.left, player) && evaluateFilter(filter.right, player)
        is PlayerFilter.Or -> evaluateFilter(filter.left, player) || evaluateFilter(filter.right, player)
        is PlayerFilter.Not -> !evaluateFilter(filter.filter, player)
    }
}