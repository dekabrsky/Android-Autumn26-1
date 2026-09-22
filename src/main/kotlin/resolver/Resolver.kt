package resolver

import model.Player
import model.Position
import model.Team

class Resolver(
    private val players: List<Player>
) : IResolver {

    override fun getCountWithoutAgency(): Int =
        players.count { it.agency == null }

    override fun getBestScorerDefender(): Pair<String, Int> {
        val best = players
            .asSequence()
            .filter { it.position == Position.DEFENDER }
            .maxWithOrNull(compareBy<Player> { it.goals }.thenBy { it.name })
            ?: return "" to 0

        return best.name to best.goals
    }

    override fun getTheExpensiveGermanPlayerPosition(): String =
        players
            .asSequence()
            .filter { it.nationality.equals("Germany", ignoreCase = true) }
            .maxByOrNull { it.transferCost }
            ?.position
            ?.russianName
            ?: ""

    override fun getTheRudestTeam(): Team =
        players
            .groupBy { it.team }
            .maxWithOrNull(
                compareBy<Map.Entry<Team, List<Player>>> {
                    it.value.map(Player::redCards).average()
                }.thenBy { it.key.name }
            )
            ?.key
            ?: error("Игроки отсутствуют")

    override fun getAverageTransferCostByPosition(): Map<Position, Double> =
        Position.entries
            .associateWith { position ->
                players
                    .filter { it.position == position }
                    .map { it.transferCost.toDouble() }
                    .average()
            }
            .filterValues { !it.isNaN() }
            .toList()
            .sortedByDescending { it.second }
            .toMap()

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
            .groupBy { it.nationality }
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
            .toSortedMap()

    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        if (players.isEmpty()) return 0.0 to 0.0

        val sortedCosts = players.map { it.transferCost }.sorted()
        val middle = sortedCosts.size / 2

        val median = if (sortedCosts.size % 2 == 0) {
            (sortedCosts[middle - 1] + sortedCosts[middle]) / 2.0
        } else {
            sortedCosts[middle].toDouble()
        }

        val allGoals = players.sumOf { it.goals }
        if (allGoals == 0) return 0.0 to 0.0

        val goalsNotAboveMedian = players
            .filter { it.transferCost <= median }
            .sumOf { it.goals }

        val lowerShare = goalsNotAboveMedian.toDouble() / allGoals
        return lowerShare to (1.0 - lowerShare)
    }
}
