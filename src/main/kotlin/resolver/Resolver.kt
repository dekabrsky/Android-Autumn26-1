package resolver

import model.Player
import model.Position
import model.Team

class Resolver(private val players: List<Player>) : IResolver {

    override fun getCountWithoutAgency(): Int =
        players.count { it.agency == null }

    override fun getBestScorerDefender(): Pair<String, Int> =
        players.filter { it.position == Position.DEFENDER }
            .maxWith(compareBy<Player> { it.goals }.thenByDescending { it.name })
            .let { it.name to it.goals }

    override fun getTheExpensiveGermanPlayerPosition(): String =
        players.filter { it.nationality == GERMANY }
            .maxBy { it.transferCost }
            .position
            .russianName

    override fun getTheRudestTeam(): Team =
        players.groupBy { it.team }
            .maxBy { (_, teamPlayers) -> teamPlayers.map { it.redCards }.average() }
            .key

    override fun getAverageTransferCostByPosition(): Map<Position, Double> =
        players.groupBy { it.position }
            .mapValues { (_, positionPlayers) -> positionPlayers.map { it.transferCost }.average() }
            .entries
            .sortedByDescending { it.value }
            .associate { it.toPair() }

    override fun getMostValuablePlayers(): List<Player> =
        players.sortedWith(compareByDescending<Player> { it.usefulness }.thenBy { it.name })
            .take(3)

    override fun getMostPopularAgencyByCountry(): Map<String, String> =
        players.mapNotNull { player -> player.agency?.let { player.nationality to it } }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, agencies) ->
                agencies.groupingBy { it }
                    .eachCount()
                    .entries
                    .minWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
                    .key
            }
            .toSortedMap()

    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        val median = players.map { it.transferCost }.median()
        val totalGoals = players.sumOf { it.goals }
        if (totalGoals == 0) return 0.0 to 0.0

        val cheapGoals = players.filter { it.transferCost <= median }.sumOf { it.goals }
        val expensiveGoals = totalGoals - cheapGoals
        return cheapGoals.toDouble() / totalGoals to expensiveGoals.toDouble() / totalGoals
    }

    private val Player.usefulness: Int
        get() = goals + 2 * assists

    private fun List<Long>.median(): Double {
        require(isNotEmpty()) { "Нельзя посчитать медиану пустого списка" }
        val sorted = sorted()
        val middle = sorted.size / 2
        return if (sorted.size % 2 == 1) {
            sorted[middle].toDouble()
        } else {
            (sorted[middle - 1] + sorted[middle]) / 2.0
        }
    }

    private companion object {
        const val GERMANY = "Germany"
    }
}
