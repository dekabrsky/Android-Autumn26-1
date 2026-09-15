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
        val player = players
            .filter { it.position == Position.DEFENDER }
            .maxWithOrNull(
                compareBy<Player> { it.goals }
                    .thenByDescending { it.name }
            )
            ?: return "" to 0

        return player.name to player.goals
    }

    override fun getTheExpensiveGermanPlayerPosition(): String {
        return players
            .asSequence()
            .filter { it.country.equals("Germany", ignoreCase = true) }
            .maxByOrNull { it.transferCost }
            ?.position
            ?.russianName
            ?: ""
    }

    override fun getTheRudestTeam(): Team {
        return players
            .groupBy { it.team }
            .maxByOrNull { (_, teamPlayers) ->
                teamPlayers.map { it.redCards }.average()
            }
            ?.key
            ?: throw NoSuchElementException("Список игроков пуст")
    }

    override fun getAverageTransferCostByPosition(): Map<Position, Double> {
        return players
            .groupBy { it.position }
            .mapValues { (_, positionPlayers) ->
                positionPlayers.map { it.transferCost }.average()
            }
            .entries
            .sortedByDescending { it.value }
            .associate { it.key to it.value }
    }

    override fun getMostValuablePlayers(): List<Player> {
        return players
            .sortedWith(
                compareByDescending<Player> {
                    it.goals + 2 * it.assists
                }.thenBy {
                    it.name
                }
            )
            .take(3)
    }

    override fun getMostPopularAgencyByCountry(): Map<String, String> {
        return players
            .filter { it.agency != null }
            .groupBy { it.country }
            .mapValues { (_, countryPlayers) ->
                countryPlayers
                    .mapNotNull { it.agency }
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
    }

    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        if (players.isEmpty()) {
            return 0.0 to 0.0
        }

        val sortedCosts = players
            .map { it.transferCost }
            .sorted()

        val size = sortedCosts.size

        val median = if (size % 2 == 1) {
            sortedCosts[size / 2].toDouble()
        } else {
            (sortedCosts[size / 2 - 1].toDouble() +
                    sortedCosts[size / 2].toDouble()) / 2.0
        }

        val totalGoals = players.sumOf { it.goals }

        if (totalGoals == 0) {
            return 0.0 to 0.0
        }

        val goalsNotMoreExpensive = players
            .filter { it.transferCost <= median }
            .sumOf { it.goals }

        val cheapShare =
            goalsNotMoreExpensive.toDouble() / totalGoals

        val expensiveShare =
            (totalGoals - goalsNotMoreExpensive).toDouble() / totalGoals

        return cheapShare to expensiveShare
    }
}