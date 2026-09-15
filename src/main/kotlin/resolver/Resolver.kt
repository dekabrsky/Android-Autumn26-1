package resolver

import model.Player
import model.Position
import model.Team
import java.util.LinkedHashMap


class Resolver(private val players: List<Player>) : IResolver {

    override fun getCountWithoutAgency(): Int =
        players.count { it.agency == null }

    override fun getBestScorerDefender(): Pair<String, Int> {
        val best = players
            .filter { it.position == Position.DEFENDER }
            .maxByOrNull { it.goals }
            ?: return "" to 0
        return best.name to best.goals
    }

    override fun getTheExpensiveGermanPlayerPosition(): String =
        players
            .filter { it.country == "Germany" }
            .maxByOrNull { it.transferCost }
            ?.position?.russianName
            ?: ""

    override fun getTheRudestTeam(): Team =
        players
            .groupBy { it.team }
            .maxByOrNull { (_, teamPlayers) -> teamPlayers.map { it.redCards }.average() }
            ?.key
            ?: Team("", "")

    override fun getAverageTransferCostByPosition(): Map<Position, Double> =
        players
            .groupBy { it.position }
            .mapValues { (_, ps) -> ps.map { it.transferCost }.average() }
            .entries
            .sortedByDescending { it.value }
            .associateTo(LinkedHashMap()) { it.key to it.value }

    override fun getMostValuablePlayers(): List<Player> =
        players
            .sortedWith(
                compareByDescending<Player> { it.goals + 2 * it.assists }
                    .thenBy { it.name }
            )
            .take(3)

    override fun getMostPopularAgencyByCountry(): Map<String, String> =
        players
            .filter { it.agency != null }
            .groupBy { it.country }
            .mapValues { (_, countryPlayers) ->
                countryPlayers
                    .groupingBy { it.agency!! }
                    .eachCount()
                    .entries
                    .sortedWith(
                        compareByDescending<Map.Entry<String, Int>> { it.value }
                            .thenBy { it.key }
                    )
                    .first()
                    .key
            }

    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        if (players.isEmpty()) return 0.0 to 0.0

        val sortedCosts = players.map { it.transferCost }.sorted()
        val median: Double = if (sortedCosts.size % 2 == 1) {
            sortedCosts[sortedCosts.size / 2].toDouble()
        } else {
            val mid = sortedCosts.size / 2
            (sortedCosts[mid - 1] + sortedCosts[mid]) / 2.0
        }

        val (cheap, expensive) = players.partition { it.transferCost <= median }

        val totalGoals = players.sumOf { it.goals }.toDouble()
        if (totalGoals == 0.0) return 0.0 to 0.0

        val cheapGoals = cheap.sumOf { it.goals }
        val expensiveGoals = expensive.sumOf { it.goals }

        return cheapGoals / totalGoals to expensiveGoals / totalGoals
    }
}
