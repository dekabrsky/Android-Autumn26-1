package resolver

import model.Player
import model.Position

sealed interface PlayerFilter {
    data class ByPosition(val position: Position) : PlayerFilter
    data class CheaperThan(val value: Long) : PlayerFilter
    data class MoreExpensiveThan(val value: Long) : PlayerFilter
    data class FromCountry(val country: String) : PlayerFilter
    data class And(val left: PlayerFilter, val right: PlayerFilter) : PlayerFilter
    data class Or(val left: PlayerFilter, val right: PlayerFilter) : PlayerFilter
    data class Not(val inner: PlayerFilter) : PlayerFilter
}

fun PlayerFilter.matches(player: Player): Boolean = when (this) {
    is PlayerFilter.ByPosition -> player.position == position
    is PlayerFilter.CheaperThan -> player.transferValue < value
    is PlayerFilter.MoreExpensiveThan -> player.transferValue > value
    is PlayerFilter.FromCountry -> player.country.equals(country, ignoreCase = true)
    is PlayerFilter.And -> left.matches(player) && right.matches(player)
    is PlayerFilter.Or -> left.matches(player) || right.matches(player)
    is PlayerFilter.Not -> !inner.matches(player)
}

object FilterExamples {

    fun defenders(players: List<Player>): List<Player> {
        val filter = PlayerFilter.ByPosition(Position.DEFENDER)
        return players.filter { filter.matches(it) }
    }

    fun expensiveGermans(players: List<Player>): List<Player> {
        val filter = PlayerFilter.And(
            PlayerFilter.FromCountry("Germany"),
            PlayerFilter.MoreExpensiveThan(30_000_000)
        )
        return players.filter { filter.matches(it) }
    }
}