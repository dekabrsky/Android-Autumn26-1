package filter

import model.Player
import model.Position

sealed interface PlayerFilter {
    fun matches(player: Player): Boolean = when (this) {
        is ByPosition -> player.position == position
        is CheaperThan -> player.transferCost < transferCost
        is And -> left.matches(player) && right.matches(player)
        is Or -> left.matches(player) || right.matches(player)
        is Not -> !filter.matches(player)
        is ByNationality -> player.nationality == nationality
        WithoutAgency -> player.agency == null
    }

    data class ByPosition(val position: Position) : PlayerFilter

    data class CheaperThan(val transferCost: Long) : PlayerFilter

    data class And(val left: PlayerFilter, val right: PlayerFilter) : PlayerFilter

    data class Or(val left: PlayerFilter, val right: PlayerFilter) : PlayerFilter

    data class Not(val filter: PlayerFilter) : PlayerFilter

    data class ByNationality(val nationality: String) : PlayerFilter

    data object WithoutAgency : PlayerFilter
}
