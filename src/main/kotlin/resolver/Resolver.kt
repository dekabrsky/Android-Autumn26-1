package resolver

import filter.PlayerFilter
import filter.matches
import model.Player
import model.Position
import model.Team

class Resolver(private val players: List<Player>) : IResolver {

    override fun getCountWithoutAgency(): Int =
        players.count { it.agency == null }

    override fun getBestScorerDefender(): Pair<String, Int> {
        val filter: PlayerFilter = PlayerFilter.ByPosition(Position.DEFENDER)

        val best = players
            .filter { filter.matches(it) }
            .maxByOrNull { it.goals }
            ?: throw NoSuchElementException("Нет защитников в данных")

        return best.name to best.goals
    }

    override fun getTheExpensiveGermanPlayerPosition(): String {
        val best = players
            .filter { it.nationality.equals("Germany", ignoreCase = true) }
            .maxByOrNull { it.transferCost }
            ?: throw NoSuchElementException("Нет немецких игроков в данных")
        return best.position.positionName
    }

    override fun getTheRudestTeam(): Team {
        return players
            .map { it.team }
            .distinct()
            .maxByOrNull { it.averageRedCards }
            ?: throw NoSuchElementException("Нет команд в данных")
    }

    override fun getAverageTransferCostByPosition(): Map<Position, Double> {
        return players
            .groupBy { it.position }
            .mapValues { (_, positionPlayers) ->
                positionPlayers.sumOf { it.transferCost }.toDouble() / positionPlayers.size
            }
            .toList()
            .sortedByDescending { it.second }
            .toMap()
    }

    override fun getMostValuablePlayers(): List<Player> {
        return players
            .sortedWith(
                compareByDescending<Player> { it.goals + 2 * it.assists }
                    .thenBy { it.name }
            )
            .take(3)
    }

    override fun getMostPopularAgencyByCountry(): Map<String, String> =
        players
            .filter { it.agency != null }
            .groupBy { it.nationality }
            .mapValues { (_, countryPlayers) ->
                countryPlayers
                    .map { requireNotNull(it.agency) }
                    .groupingBy { it }
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
        val sortedCosts = players.map { it.transferCost }.sorted()
        val n = sortedCosts.size

        val median: Double = if (n == 0) {
            0.0
        } else if (n % 2 == 1) {
            sortedCosts[n / 2].toDouble()
        } else {
            (sortedCosts[n / 2 - 1] + sortedCosts[n / 2]) / 2.0
        }

        val threshold = kotlin.math.floor(median).toLong() + 1

        val cheapFilter: PlayerFilter = PlayerFilter.CheaperThan(threshold)
        val expensiveFilter: PlayerFilter = PlayerFilter.Not(cheapFilter)

        val cheapOrEqualGoals = players
            .filter { cheapFilter.matches(it) }
            .sumOf { it.goals }

        val expensiveGoals = players
            .filter { expensiveFilter.matches(it) }
            .sumOf { it.goals }

        val totalGoals = cheapOrEqualGoals + expensiveGoals

        return if (totalGoals == 0) {
            0.0 to 0.0
        } else {
            (cheapOrEqualGoals.toDouble() / totalGoals) to (expensiveGoals.toDouble() / totalGoals)
        }
    }
}