package resolver

import model.Player
import model.Position
import model.Team

class Resolver(
    private val players: List<Player>,
) : IResolver {

    override fun getCountWithoutAgency(): Int =
        players.count { it.agency == null }

    override fun getBestScorerDefender(): Pair<String, Int> {
        val defender = players
            .filter { it.position == Position.DEFENDER }
            .sortedWith(compareByDescending<Player> { it.goals }.thenBy { it.name })
            .firstOrNull() ?: error("No defenders found")

        return defender.name to defender.goals
    }

    override fun getTheExpensiveGermanPlayerPosition(): String {
        val player = players
            .filter { it.nationality == "Germany" }
            .maxByOrNull { it.transferCost } ?: error("No German players found")

        return player.position.russianName
    }

    override fun getTheRudestTeam(): Team =
        players
            .groupBy { it.team }
            .entries
            .sortedWith(
                compareByDescending<Map.Entry<Team, List<Player>>> { (_, teamPlayers) ->
                    teamPlayers.averageRedCards()
                }.thenBy { it.key.name }.thenBy { it.key.city },
            )
            .firstOrNull()
            ?.key ?: error("No teams found")

    override fun getAverageTransferCostByPosition(): Map<Position, Double> =
        players
            .groupBy { it.position }
            .mapValues { (_, positionPlayers) -> positionPlayers.map { it.transferCost }.average() }
            .entries
            .sortedWith(compareByDescending<Map.Entry<Position, Double>> { it.value }.thenBy { it.key.name })
            .associate { it.key to it.value }

    override fun getMostValuablePlayers(): List<Player> =
        players
            .sortedWith(compareByDescending<Player> { it.goals + 2 * it.assists }.thenBy { it.name })
            .take(3)

    override fun getMostPopularAgencyByCountry(): Map<String, String> =
        players
            .filter { it.agency != null }
            .groupBy { it.nationality }
            .entries
            .sortedBy { it.key }
            .associate { (country, countryPlayers) ->
                country to countryPlayers
                    .groupingBy { requireNotNull(it.agency) }
                    .eachCount()
                    .entries
                    .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
                    .first()
                    .key
            }

    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        if (players.isEmpty()) return 0.0 to 0.0

        val sortedCosts = players.map { it.transferCost }.sorted()
        val median = if (sortedCosts.size % 2 == 1) {
            sortedCosts[sortedCosts.size / 2].toDouble()
        } else {
            val rightMiddleIndex = sortedCosts.size / 2
            (sortedCosts[rightMiddleIndex - 1].toDouble() + sortedCosts[rightMiddleIndex]) / 2.0
        }

        val totalGoals = players.sumOf { it.goals }
        if (totalGoals == 0) return 0.0 to 0.0

        val cheaperOrMedianGoals = players
            .filter { it.transferCost <= median }
            .sumOf { it.goals }

        return cheaperOrMedianGoals.toDouble() / totalGoals to
            (totalGoals - cheaperOrMedianGoals).toDouble() / totalGoals
    }

    private fun List<Player>.averageRedCards(): Double =
        sumOf { it.redCards }.toDouble() / size
}
