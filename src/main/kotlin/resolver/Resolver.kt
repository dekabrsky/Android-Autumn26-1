package resolver

import model.Player
import model.Position
import model.Team
import resolver.IResolver

class Resolver(private val players: List<Player>) : IResolver {

    override fun getCountWithoutAgency(): Int = players.count { it.agency == null }

    override fun getBestScorerDefender(): Pair<String, Int> {
        return players
            .filter { it.position == Position.DEFENDER }
            .maxByOrNull { it.goals }
            ?.let { it.name to it.goals }
            ?: error("Ноль защитников")
    }

    override fun getTheExpensiveGermanPlayerPosition(): String {
        return players
            .filter { it.nationality == "Germany"}
            .maxBy { it.transferCost }
            .position
            .className
    }

    override fun getTheRudestTeam(): Team {
        return players
            .groupBy { it.team }
            .maxBy { (_, teamPlayers) ->
                teamPlayers.map { it.redCards }.average()
            }
            .key
    }

    override fun getAverageTransferCostByPosition(): Map<Position, Double> {
        return players
            .groupBy { it.position }
            .mapValues { (_, players) ->
                players.map { it.transferCost }.average()
            }
            .toList()
            .sortedByDescending { it.second }
            .toMap()
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
            .groupBy { it.nationality }
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
        val sortedCosts = players
            .map { it.transferCost }
            .sorted()

        val median = if (sortedCosts.size % 2 == 1) {
            sortedCosts[sortedCosts.size / 2].toDouble()
        } else {
            val middle = sortedCosts.size / 2

            (sortedCosts[middle - 1] + sortedCosts[middle]) / 2.0
        }

        val goalsNotMoreExpensive = players
            .filter { it.transferCost <= median }
            .sumOf { it.goals }

        val goalsMoreExpensive = players
            .filter { it.transferCost > median }
            .sumOf { it.goals }

        val totalGoals = players.sumOf { it.goals }

        return if (totalGoals == 0) {
            0.0 to 0.0
        } else {
            goalsNotMoreExpensive.toDouble() / totalGoals to
                    goalsMoreExpensive.toDouble() / totalGoals
        }
    }
}