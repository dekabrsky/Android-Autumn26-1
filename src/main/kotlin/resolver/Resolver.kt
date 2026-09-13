package resolver

import filter.PlayerFilter
import filter.filterBy
import model.Player
import model.Position
import model.Team

class Resolver(private val players: List<Player>) : IResolver {

    override fun getCountWithoutAgency(): Int {
        return players.count { it.agency == null }
    }

    override fun getBestScorerDefender(): Pair<String, Int> {
        // через фильтр ByPosition (задание 11)
        val defenders = players.filterBy(PlayerFilter.ByPosition(Position.DEFENDER))
        val best = defenders.maxBy { it.goals }
        return best.name to best.goals
    }

    override fun getTheExpensiveGermanPlayerPosition(): String {
        val german = players
            .filter { it.nationality == "Germany" }
            .maxBy { it.transferCost }
        return german.position.russianName
    }

    override fun getTheRudestTeam(): Team {
        // собираем команды с игроками, чтобы воспользоваться lazy-свойством
        return players
            .groupBy { it.team.name to it.team.city }
            .map { (key, teamPlayers) ->
                Team(key.first, key.second, teamPlayers)
            }
            .maxBy { it.averageRedCards }
    }

    override fun getAverageTransferCostByPosition(): Map<Position, Double> {
        return players
            .groupBy { it.position }
            .mapValues { (_, group) -> group.map { it.transferCost }.average() }
            .toList()
            .sortedByDescending { it.second }
            .toMap()
    }

    override fun getMostValuablePlayers(): List<Player> {
        fun usefulness(p: Player) = p.goals + 2 * p.assists

        return players
            .sortedWith(
                compareByDescending<Player> { usefulness(it) }
                    .thenBy { it.name }
            )
            .take(3)
    }

    override fun getMostPopularAgencyByCountry(): Map<String, String> {
        return players
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
    }

    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        val costs = players.map { it.transferCost }.sorted()
        val n = costs.size
        val median = if (n % 2 == 0) {
            (costs[n / 2 - 1] + costs[n / 2]) / 2.0
        } else {
            costs[n / 2].toDouble()
        }

        val totalGoals = players.sumOf { it.goals }.toDouble()
        if (totalGoals == 0.0) return 0.0 to 0.0

        // через фильтры CheaperThan / Not (задание 11)
        // для целых стоимостей: cost <= median  <=>  !(cost > median)
        // CheaperThan берём как cost < bound, поэтому bound = floor(median) + 1
        val bound = kotlin.math.floor(median).toLong() + 1
        val notAboveMedian = PlayerFilter.CheaperThan(bound)
        val aboveMedian = PlayerFilter.Not(notAboveMedian)

        val goalsCheap = players.filterBy(notAboveMedian).sumOf { it.goals }
        val goalsExpensive = players.filterBy(aboveMedian).sumOf { it.goals }

        return (goalsCheap / totalGoals) to (goalsExpensive / totalGoals)
    }
}
