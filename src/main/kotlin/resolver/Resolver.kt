package resolver

import model.Player
import model.Position
import model.Team
import model.toTeams

class Resolver(private val players: List<Player>) : IResolver {

    // 1
    override fun getCountWithoutAgency(): Int = players.count { it.agency == null }

    // 2
    override fun getBestScorerDefender(): Pair<String, Int> =
        players.filter { it.position == Position.DEFENDER }
            .sortedWith(compareByDescending<Player> { it.goals }.thenBy { it.name })
            .first()
            .let { it.name to it.goals }

    // 3
    override fun getTheExpensiveGermanPlayerPosition(): String =
        players.filter { it.country == "Germany" }
            .maxBy { it.transferCost }
            .position.russianName

    // 4 (использует лениво посчитанное Team.averageRedCards)
    override fun getTheRudestTeam(): Team =
        players.toTeams()
            .sortedWith(compareByDescending<Team> { it.averageRedCards }.thenBy { it.name })
            .first()

    // 5
    override fun getAverageTransferCostByPosition(): Map<Position, Double> =
        players.groupBy { it.position }
            .mapValues { (_, group) -> group.map { it.transferCost }.average() }
            .entries
            .sortedByDescending { it.value }
            .associate { it.key to it.value } // associate сохраняет порядок (LinkedHashMap)

    // 6
    override fun getMostValuablePlayers(): List<Player> =
        players.sortedWith(compareByDescending<Player> { it.utility }.thenBy { it.name })
            .take(3)

    // 7
    override fun getMostPopularAgencyByCountry(): Map<String, String> =
        players.groupBy { it.country }
            .mapNotNull { (country, group) ->
                val agency = group.mapNotNull { it.agency }
                    .groupingBy { it }
                    .eachCount()
                    .entries
                    .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
                    .firstOrNull()
                    ?.key
                agency?.let { country to it }
            }
            .toMap()

    // 8
    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        if (players.isEmpty()) return 0.0 to 0.0
        val median = medianOf(players.map { it.transferCost })
        val totalGoals = players.sumOf { it.goals }
        if (totalGoals == 0) return 0.0 to 0.0
        val cheapGoals = players.filter { it.transferCost <= median }.sumOf { it.goals }
        val cheapShare = cheapGoals.toDouble() / totalGoals
        return cheapShare to (1.0 - cheapShare)
    }

    companion object {
        fun medianOf(values: List<Long>): Double {
            val sorted = values.sorted()
            val mid = sorted.size / 2
            return if (sorted.size % 2 == 1) sorted[mid].toDouble()
            else (sorted[mid - 1] + sorted[mid]) / 2.0
        }
    }
}
