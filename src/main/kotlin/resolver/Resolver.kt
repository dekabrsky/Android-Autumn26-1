package resolver

import model.ByPosition
import model.Player
import model.Position
import model.Team

class Resolver(private val players: List<Player>) : IResolver {

    override fun getCountWithoutAgency(): Int {
        return players.count { it.agency == null }
    }

    override fun getBestScorerDefender(): Pair<String, Int> {
        val defenderFilter = ByPosition(Position.DEFENDER)

        val bestDefender = players
            .filter { defenderFilter.matches(it) }
            .maxByOrNull { it.goals }

        return bestDefender?.let { it.name to it.goals } ?: Pair("", 0)
    }

    override fun getTheExpensiveGermanPlayerPosition(): String {
        return players
            .filter { it.country.equals("Germany", ignoreCase = true) }
            .maxByOrNull { it.marketValue }
            ?.position?.russianName ?: "Не найден"
    }

    override fun getTheRudestTeam(): Team {
        return players.maxByOrNull { it.team.avgRedCards }?.team
            ?: throw IllegalStateException("Нет команд")
    }

    override fun getAverageTransferCostByPosition(): Map<Position, Double> {
        return players.groupBy { it.position }
            .mapValues { (_, posPlayers) -> posPlayers.map { it.marketValue }.average() }
            .entries
            .sortedByDescending { it.value }
            .associate { it.key to it.value }
    }

    override fun getMostValuablePlayers(): List<Player> {
        return players.sortedWith(
            compareByDescending<Player> { it.goals + 2 * it.assists }
                .thenBy { it.name }
        ).take(3)
    }

    override fun getMostPopularAgencyByCountry(): Map<String, String> {
        return players.filter { it.agency != null }
            .groupBy { it.country }
            .mapValues { (_, countryPlayers) ->
                countryPlayers.groupBy { it.agency!! }
                    .entries
                    .sortedWith(
                        compareByDescending<Map.Entry<String, List<Player>>> { it.value.size }
                            .thenBy { it.key }
                    )
                    .first().key
            }
    }

    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        if (players.isEmpty()) return Pair(0.0, 0.0)

        val sortedCosts = players.map { it.marketValue }.sorted()
        val size = sortedCosts.size
        val median = if (size % 2 == 0) {
            (sortedCosts[size / 2 - 1] + sortedCosts[size / 2]) / 2.0
        } else {
            sortedCosts[size / 2].toDouble()
        }

        val totalGoals = players.sumOf { it.goals }.toDouble()
        if (totalGoals == 0.0) return Pair(0.0, 0.0)

        val (cheapPlayers, expensivePlayers) = players.partition { it.marketValue <= median }
        val cheapGoals = cheapPlayers.sumOf { it.goals }.toDouble()
        val expensiveGoals = expensivePlayers.sumOf { it.goals }.toDouble()

        return Pair(cheapGoals / totalGoals, expensiveGoals / totalGoals)
    }
}