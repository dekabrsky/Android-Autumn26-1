package resolver

import filter.PlayerFilter
import filter.filterBy
import filter.matches
import model.Player
import model.Position
import model.Team

class Resolver(private val players: List<Player>) : IResolver {

    override fun getCountWithoutAgency(): Int =
        players.count { it.agency == null }

    override fun getBestScorerDefender(): Pair<String, Int> {
        val defenders = players.filterBy(PlayerFilter.ByPosition(Position.DEFENDER))
        val best = requireNotNull(defenders.maxByOrNull { it.goals }) {
            "В выборке нет защитников"
        }
        return best.name to best.goals
    }

    override fun getTheExpensiveGermanPlayerPosition(): String {
        val bestGerman = requireNotNull(
            players.filter { it.country == "Germany" }.maxByOrNull { it.transferCost }
        ) { "В выборке нет немецких игроков" }
        return bestGerman.position.russianName
    }

    override fun getTheRudestTeam(): Team {
        val teams = players.map { it.team }.distinct()
        return requireNotNull(teams.maxByOrNull { it.averageRedCards }) {
            "В выборке нет команд"
        }
    }

    override fun getAverageTransferCostByPosition(): Map<Position, Double> =
        Position.entries
            .associateWith { position ->
                players.filterBy(PlayerFilter.ByPosition(position))
            }
            .filterValues { it.isNotEmpty() }
            .mapValues { (_, ps) -> ps.map { it.transferCost.toDouble() }.average() }
            .toList()
            .sortedByDescending { it.second }
            .toMap()

    override fun getMostValuablePlayers(): List<Player> =
        players
            .sortedWith(compareByDescending<Player> { it.usefulness }.thenBy { it.name })
            .take(3)

    override fun getMostPopularAgencyByCountry(): Map<String, String> =
        players.groupBy { it.country }
            .mapNotNull { (country, countryPlayers) ->
                val popularAgency = countryPlayers
                    .mapNotNull { it.agency }
                    .groupingBy { it }
                    .eachCount()
                    .entries
                    .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
                    .firstOrNull()
                    ?.key
                popularAgency?.let { country to it }
            }
            .toMap()

    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        if (players.isEmpty()) return 0.0 to 0.0

        val sortedCosts = players.map { it.transferCost }.sorted()
        val size = sortedCosts.size
        val median = if (size % 2 == 0) {
            (sortedCosts[size / 2 - 1] + sortedCosts[size / 2]) / 2.0
        } else {
            sortedCosts[size / 2].toDouble()
        }

        val totalGoals = players.sumOf { it.goals }
        if (totalGoals == 0) return 0.0 to 0.0

        val goalsNotAboveMedian = players.filter { it.transferCost <= median }.sumOf { it.goals }
        val goalsAboveMedian = totalGoals - goalsNotAboveMedian

        return (goalsNotAboveMedian.toDouble() / totalGoals) to (goalsAboveMedian.toDouble() / totalGoals)
    }

    fun countCheapDefenders(maxCost: Long): Int {
        val filter = PlayerFilter.And(PlayerFilter.ByPosition(Position.DEFENDER), PlayerFilter.CheaperThan(maxCost))
        return players.count { filter.matches(it) }
    }

    fun countNonForwardsCheaperThan(maxCost: Long): Int {
        val filter = PlayerFilter.And(PlayerFilter.Not(PlayerFilter.ByPosition(Position.FORWARD)), PlayerFilter.CheaperThan(maxCost))
        return players.count { filter.matches(it) }
    }
}
