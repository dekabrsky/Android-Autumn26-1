package filter

import model.Player
import model.Position

sealed interface PlayerFilter {
    data class ByPosition(val position: Position) : PlayerFilter
    data class CheaperThan(val limit: Long) : PlayerFilter
    data class And(val left: PlayerFilter, val right: PlayerFilter) : PlayerFilter
    data class Or(val left: PlayerFilter, val right: PlayerFilter) : PlayerFilter
    data class Not(val filter: PlayerFilter) : PlayerFilter

    /** Исчерпывающий when без else: новый наследник не скомпилируется, пока его не обработают. */
    fun matches(player: Player): Boolean = when (this) {
        is ByPosition -> player.position == position
        is CheaperThan -> player.transferCost < limit
        is And -> left.matches(player) && right.matches(player)
        is Or -> left.matches(player) || right.matches(player)
        is Not -> !filter.matches(player)
    }
}

infix fun PlayerFilter.and(other: PlayerFilter): PlayerFilter = PlayerFilter.And(this, other)
infix fun PlayerFilter.or(other: PlayerFilter): PlayerFilter = PlayerFilter.Or(this, other)
operator fun PlayerFilter.not(): PlayerFilter = PlayerFilter.Not(this)

fun List<Player>.select(filter: PlayerFilter): List<Player> = filter { filter.matches(it) }

// Запросы из задания 2, переформулированные через фильтры

/** Запрос 2: лучший бомбардир среди защитников. */
fun bestDefenderScorer(players: List<Player>): Pair<String, Int> =
    players.select(PlayerFilter.ByPosition(Position.DEFENDER))
        .sortedWith(compareByDescending<Player> { it.goals }.thenBy { it.name })
        .first().let { it.name to it.goals }

/** Запрос 5 (частично): средняя стоимость полевых игроков дешевле лимита. */
fun averageCostOfFieldPlayersBelow(players: List<Player>, limit: Long): Double =
    players.select(!PlayerFilter.ByPosition(Position.GOALKEEPER) and PlayerFilter.CheaperThan(limit))
        .map { it.transferCost }
        .average()
