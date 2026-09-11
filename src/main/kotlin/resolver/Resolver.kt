package resolver

import filter.ByPosition
import model.Player
import model.Position
import model.Team

class Resolver(private val players: List<Player>) : IResolver {

    override fun getCountWithoutAgency(): Int = players.count { it.agency == null }

    override fun getBestScorerDefender(): Pair<String, Int> {
        val defenders = ByPosition(Position.DEFENDER)
        return players.asSequence()
            .filter { defenders.matches(it) }
            .maxByOrNull { it.goals }
            ?.let { it.name to it.goals }
            ?: error("There are no defenders in the dataset")
    }

    override fun getTheExpensiveGermanPlayerPosition(): String =
        players.asSequence()
            .filter { it.nationality.equals("Germany", ignoreCase = true) }
            .maxByOrNull { it.transferCost }
            ?.position
            ?.russianName
            ?: error("There are no German players in the dataset")

    override fun getTheRudestTeam(): Team =
        players.map(Player::team)
            .distinct()
            .maxWithOrNull(compareBy<Team> { it.averageRedCards }.thenBy(Team::name).thenBy(Team::city))
            ?: error("There are no teams in the dataset")

    override fun getAverageTransferCostByPosition(): Map<Position, Double> =
        players.groupBy(Player::position)
            .mapValues { (_, positionPlayers) -> positionPlayers.map(Player::transferCost).average() }
            .entries
            .sortedWith(compareByDescending<Map.Entry<Position, Double>> { it.value }.thenBy { it.key.name })
            .associate { it.toPair() }

    override fun getMostValuablePlayers(): List<Player> =
        players.sortedWith(
            compareByDescending<Player> { it.goals + 2 * it.assists }
                .thenBy(Player::name),
        ).take(3)

    override fun getMostPopularAgencyByCountry(): Map<String, String> =
        players.filter { it.agency != null }
            .groupBy(Player::nationality)
            .mapValues { (_, countryPlayers) ->
                countryPlayers.map { requireNotNull(it.agency) }
                    .groupingBy { it }
                    .eachCount()
                    .entries
                    .sortedWith(
                        compareByDescending<Map.Entry<String, Int>> { it.value }
                            .thenBy { it.key },
                    )
                    .first()
                    .key
            }

    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        if (players.isEmpty()) return 0.0 to 0.0

        val costs = players.map(Player::transferCost).sorted()
        val middle = costs.size / 2
        val median = if (costs.size % 2 == 0) {
            (costs[middle - 1].toDouble() + costs[middle]) / 2
        } else {
            costs[middle].toDouble()
        }

        val totalGoals = players.sumOf(Player::goals)
        if (totalGoals == 0) return 0.0 to 0.0

        val goalsAtOrBelowMedian = players
            .filter { it.transferCost <= median }
            .sumOf(Player::goals)
        val belowShare = goalsAtOrBelowMedian.toDouble() / totalGoals
        return belowShare to 1.0 - belowShare
    }
}
