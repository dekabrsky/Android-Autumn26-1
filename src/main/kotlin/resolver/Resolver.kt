package resolver

import filter.ByPosition
import filter.matches
import model.Player
import model.Position
import model.Team

class Resolver(private val players: List<Player>) : IResolver {
    override fun getCountWithoutAgency(): Int = players.count { it.agency == null }

    override fun getBestScorerDefender(): Pair<String, Int> {
        val defenderFilter = ByPosition(Position.DEFENDER)
        val player = players
            .asSequence()
            .filter(defenderFilter::matches)
            .sortedWith(compareByDescending<Player> { it.goalsCount }.thenBy { it.name })
            .firstOrNull()
            ?: return "" to 0

        return player.name to player.goalsCount
    }

    override fun getTheExpensiveGermanPlayerPosition(): String = players
        .asSequence()
        .filter { it.country.equals("Germany", ignoreCase = true) }
        .sortedWith(compareByDescending<Player> { it.transferCost }.thenBy { it.name })
        .firstOrNull()
        ?.position
        ?.russianName
        .orEmpty()

    override fun getTheRudestTeam(): Team = players
        .map { it.team }
        .distinct()
        .sortedWith(compareByDescending<Team> { it.averageRedCards }.thenBy { it.name }.thenBy { it.city })
        .firstOrNull()
        ?: throw NoSuchElementException("Невозможно определить команду: список игроков пуст")

    override fun getAverageTransferCostByPosition(): Map<Position, Double> = Position.entries
        .mapNotNull { position ->
            val positionFilter = ByPosition(position)
            val costs = players.filter(positionFilter::matches).map { it.transferCost }
            costs.takeIf { it.isNotEmpty() }?.let { position to it.average() }
        }
        .sortedWith(compareByDescending<Pair<Position, Double>> { it.second }.thenBy { it.first.name })
        .associateTo(linkedMapOf()) { it }

    override fun getMostValuablePlayers(): List<Player> = players
        .sortedWith(
            compareByDescending<Player> { it.goalsCount + 2 * it.goalPassesCount }
                .thenBy { it.name },
        )
        .take(3)

    override fun getMostPopularAgencyByCountry(): Map<String, String> = players
        .groupBy { it.country }
        .toSortedMap()
        .mapNotNull { (country, countryPlayers) ->
            countryPlayers
                .mapNotNull { it.agency }
                .groupingBy { it }
                .eachCount()
                .entries
                .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
                .firstOrNull()
                ?.let { country to it.key }
        }
        .toMap(linkedMapOf())

    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        if (players.isEmpty()) return 0.0 to 0.0

        val sortedCosts = players.map { it.transferCost }.sorted()
        val middle = sortedCosts.size / 2
        val median = if (sortedCosts.size % 2 == 0) {
            (sortedCosts[middle - 1].toLong() + sortedCosts[middle]) / 2.0
        } else {
            sortedCosts[middle].toDouble()
        }

        val allGoals = players.sumOf { it.goalsCount }
        if (allGoals == 0) return 0.0 to 0.0

        val cheaperGoals = players
            .filter { it.transferCost <= median }
            .sumOf { it.goalsCount }
        val cheaperShare = cheaperGoals.toDouble() / allGoals
        return cheaperShare to (1.0 - cheaperShare)
    }
}
