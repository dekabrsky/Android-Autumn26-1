package resolver

import model.Player
import model.Position
import model.Team

class Resolver(
    private val players: List<Player>,
) : IResolver {

    override fun getCountWithoutAgency(): Int =
        players.count { it.agency == null }

    override fun getBestScorerDefender(): Pair<String, Int> =
        players.filter { it.position == Position.DEFENDER }
            .maxWithOrNull(compareBy<Player> { it.goals }.thenByDescending { it.name })
            ?.let { it.name to it.goals }
            ?: ("" to 0)

    override fun getTheExpensiveGermanPlayerPosition(): String =
        players.filter { it.country == "Germany" }
            .maxByOrNull { it.transferCost }
            ?.position
            ?.russianName
            .orEmpty()

    override fun getTheRudestTeam(): Team =
        players.groupBy { it.team }
            .maxByOrNull { (_, teamPlayers) -> teamPlayers.map { it.redCards }.average() }
            ?.key
            ?: Team("", "")

    override fun getAverageTransferCostByPosition(): Map<Position, Double> =
        players.groupBy { it.position }
            .mapValues { (_, positionPlayers) -> positionPlayers.map { it.transferCost }.average() }
            .toList()
            .sortedByDescending { it.second }
            .toMap(LinkedHashMap())

    override fun getMostValuablePlayers(): List<Player> =
        players.sortedWith(
            compareByDescending<Player> { it.goals + 2 * it.assists }
                .thenBy { it.name },
        ).take(3)

    override fun getMostPopularAgencyByCountry(): Map<String, String> =
        players.groupBy { it.country }
            .mapNotNull { (country, countryPlayers) ->
                val agency = countryPlayers.mapNotNull { it.agency }
                    .groupingBy { it }
                    .eachCount()
                    .toList()
                    .sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first })
                    .firstOrNull()
                    ?.first

                agency?.let { country to it }
            }
            .toMap(LinkedHashMap())

    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        if (players.isEmpty()) return 0.0 to 0.0

        val costs = players.map { it.transferCost }.sorted()
        val middle = costs.size / 2
        val median = if (costs.size % 2 == 0) {
            (costs[middle - 1] + costs[middle]) / 2.0
        } else {
            costs[middle].toDouble()
        }

        val lowerGoals = players.filter { it.transferCost <= median }.sumOf { it.goals }
        val higherGoals = players.filter { it.transferCost > median }.sumOf { it.goals }
        val totalGoals = lowerGoals + higherGoals

        if (totalGoals == 0) return 0.0 to 0.0

        return lowerGoals.toDouble() / totalGoals to higherGoals.toDouble() / totalGoals
    }
}
