package resolver

import model.Player
import model.Position
import model.Team
import parser.CsvParser

class Resolver(private val allPlayers: List<Player> = CsvParser.parse().players) : IResolver {

    override fun getCountWithoutAgency(): Int =
        allPlayers.count { it.agency == null }

    override fun getBestScorerDefender(): Pair<String, Int> {
        val best = allPlayers
            .filter { it.position == Position.DEFENDER }
            .maxByOrNull { it.goals }
        return if (best != null) Pair(best.name, best.goals) else Pair("", -1)
    }

    override fun getTheExpensiveGermanPlayerPosition(): String {
        return allPlayers
            .filter { it.nationality == "Germany" }
            .maxByOrNull { it.transferCost }
            ?.position?.russianName ?: ""
    }

    override fun getTheRudestTeam(): Team {
        return allPlayers
            .groupBy { it.team }
            .maxByOrNull { (_, players) ->
                if (players.isEmpty()) 0.0 else players.sumOf { it.redCards }.toDouble() / players.size
            }
            ?.key ?: throw NoSuchElementException("No teams found")
    }

    override fun getAverageTransferCostByPosition(): Map<Position, Double> {
        return allPlayers
            .groupBy { it.position }
            .mapValues { (_, players) -> players.sumOf { it.transferCost }.toDouble() / players.size }
            .entries
            .sortedByDescending { it.value }
            .associate { it.key to it.value }
    }

    override fun getMostValuablePlayers(): List<Player> {
        return allPlayers
            .sortedWith(compareByDescending<Player> { it.goals + 2 * it.assists }.thenBy { it.name })
            .take(3)
    }

    override fun getMostPopularAgencyByCountry(): Map<String, String> {
        return allPlayers
            .filter { it.agency != null }
            .groupBy { it.nationality }
            .mapValues { (_, players) ->
                players
                    .groupingBy { it.agency!! }
                    .eachCount()
                    .entries
                    .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
                    .first()
                    .key
            }
    }

    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        if (allPlayers.isEmpty()) return Pair(0.0, 0.0)

        val costs = allPlayers.map { it.transferCost }.sorted()
        val n = costs.size
        val median = if (n % 2 == 1) {
            costs[n / 2].toDouble()
        } else {
            (costs[n / 2 - 1] + costs[n / 2]) / 2.0
        }

        val totalGoals = allPlayers.sumOf { it.goals }
        if (totalGoals == 0) return Pair(0.0, 0.0)

        val (lowGoals, highGoals) = allPlayers
            .partition { it.transferCost <= median }
            .let { (low, high) -> low.sumOf { it.goals } to high.sumOf { it.goals } }

        return Pair(lowGoals.toDouble() / totalGoals, highGoals.toDouble() / totalGoals)
    }
}
