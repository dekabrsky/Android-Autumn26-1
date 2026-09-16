package grath

import model.Player
import model.Position

fun topTeamsByTotalValue(players: List<Player>, n: Int = 10): List<Pair<String, Long>> =
    players.groupBy { it.team }
        .map { (team, list) ->
            "${team.name} (${team.city})" to list.sumOf { it.transferValue }
        }
        .sortedByDescending { it.second }
        .take(n)

data class StrikerPoint(
    val value: Long,
    val goals: Int,
    val yellowCards: Int
)

fun strikersPoints(players: List<Player>): List<StrikerPoint> =
    players.asSequence()
        .filter { it.position == Position.FORWARD }
        .map { StrikerPoint(it.transferValue, it.goals, it.yellowCards) }
        .toList()

fun movingAverageTrend(
    points: List<StrikerPoint>,
    window: Int = 7
): List<Pair<Long, Double>> {
    if (points.isEmpty() || window <= 0) return emptyList()
    val sortedByValue = points.sortedBy { it.value }
    return sortedByValue
        .windowed(window, partialWindows = false) { chunk ->
            val avgValue = chunk.map { it.value }.average().toLong()
            val avgGoals = chunk.map { it.goals }.average()
            avgValue to avgGoals
        }
}