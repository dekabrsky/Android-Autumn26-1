package resolver

import model.Player
import model.Position
import model.Team

class Resolver(
    private val players: List<Player>
) : IResolver {

    override fun getCountWithoutAgency(): Int {
        return players.count { it.agency == null }
    }

    override fun getBestScorerDefender(): Pair<String, Int> {
        return players
            .filter { it.position == Position.DEFENDER }
            .maxByOrNull { it.goals }
            ?.let { it.name to it.goals }
            ?: throw RuntimeException("Defenders not found")
    }

    override fun getTheExpensiveGermanPlayerPosition(): String {
        return players
            .filter { it.nationality == "Germany" }
            .maxByOrNull { it.transferCost }
            ?.position
            ?.russianName
            ?: throw RuntimeException("Players from Germany not found")
    }

    override fun getTheRudestTeam(): Team {
        return players
            .groupBy { it.team }
            .maxByOrNull { (_, teamPlayers) ->
                teamPlayers.map { it.redCards }.average()
            }
            ?.key
            ?: throw RuntimeException("Teams not found")
    }

    override fun getAverageTransferCostByPosition(): Map<Position, Double> {
        return players
            .groupBy { it.position }
            .mapValues { (_, players) ->
                players.map { it.transferCost }.average()
            }
            .toList()
            .sortedByDescending { (_, averageCost) -> averageCost }
            .toMap()
    }

    override fun getMostValuablePlayers(): List<Player> {
        return players
            .sortedWith(
                compareByDescending<Player> {
                    it.goals + 2 * it.assists
                }.thenBy { it.name }
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
                    .maxWithOrNull(
                        compareBy<Map.Entry<String, Int>> { it.value }
                            .thenByDescending { it.key }
                    )
                    ?.key
                    ?: ""
            }
    }

    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        val sortedCosts = players
            .map { it.transferCost }
            .sorted()

        val median = if (sortedCosts.size % 2 == 0) {
            (sortedCosts[sortedCosts.size / 2 - 1] + sortedCosts[sortedCosts.size / 2]) / 2.0
        } else {
            sortedCosts[sortedCosts.size / 2].toDouble()
        }

        val totalGoals = players.sumOf { it.goals }
        if (totalGoals == 0) {
            return 0.0 to 0.0
        }

        val goalsNotMoreThanMedian = players
            .filter { it.transferCost <= median }
            .sumOf { it.goals }

        val goalsMoreThanMedian = totalGoals - goalsNotMoreThanMedian

        return goalsNotMoreThanMedian.toDouble() / totalGoals to
                goalsMoreThanMedian.toDouble() / totalGoals
    }
}