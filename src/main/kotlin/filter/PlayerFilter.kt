package filter

import model.Player
import model.Position

sealed interface PlayerFilter {
    fun matches(player: Player): Boolean = when (this) {
        is ByPosition -> player.position == position
        is CheaperThan -> player.transferCost < maxCost
        is And -> left.matches(player) && right.matches(player)
        is Or -> left.matches(player) || right.matches(player)
        is Not -> !filter.matches(player)
    }
}

class ByPosition(val position: Position) : PlayerFilter

class CheaperThan(val maxCost: Int) : PlayerFilter

class And(val left: PlayerFilter, val right: PlayerFilter) : PlayerFilter

class Or(val left: PlayerFilter, val right: PlayerFilter) : PlayerFilter

class Not(val filter: PlayerFilter) : PlayerFilter