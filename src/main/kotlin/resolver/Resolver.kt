package resolver

import model.Player
import model.Position
import model.Team
import utility.PlayerFilter
import utility.evaluateFilter

class Resolver(private val players: List<Player>) : IResolver {
    override fun getCountWithoutAgency(): Int {
        return players.count { it.agency == null }
    }

    override fun getBestScorerDefender(): Pair<String, Int> {
        val defenderFilter = PlayerFilter.ByPosition(Position.DEFENDER)
        val bestDefender = players
            .filter { evaluateFilter(defenderFilter, it) }
            .maxByOrNull { it.goals }

        return if(bestDefender != null)
            Pair(bestDefender.name, bestDefender.goals)
        else
            Pair("", 0)
    }

    override fun getTheExpensiveGermanPlayerPosition(): String {
        val mostExpensiveGerman = players
            .filter { it.nationality == "Germany" }
            .maxByOrNull { it.transferCost }

        return mostExpensiveGerman?.position?.displayName ?: "Не найдено"
    }

    override fun getTheRudestTeam(): Team {
        return players
            .groupBy { it.team }
            .map { (team, list) -> Team(team.name, team.city, list) }
            .maxByOrNull { it.averageRedCards } ?: Team("", "")
    }

    override fun getAverageTransferCostByPosition(): Map<Position, Double> {
        return players
            .groupBy { it.position }
            .mapValues { (_, positionPlayers) ->
                positionPlayers.map { it.transferCost.toDouble() }.average()
            }
            .toList()
            .sortedByDescending { (_, avgCost) -> avgCost }
            .toMap()
    }

    override fun getMostValuablePlayers(): List<Player> {
        return players
            .sortedWith(
                compareByDescending<Player> { it.goals + 2 * it.assists }
                    .thenBy  { it.name }
            )
            .take(3)
    }

    override fun getMostPopularAgencyByCountry(): Map<String, String> {
        return players
            .filter { it.agency != null }
            .groupBy { it.nationality }
            .mapValues { (_, countPlayers) ->
                countPlayers
                    .groupBy { it.agency!! }
                    .mapValues { (_, agencyPlayers) -> agencyPlayers.size }
                    .maxWithOrNull(
                        compareBy<Map.Entry<String, Int>> { it.value }
                            .thenByDescending { it.key }
                    )?.key ?: ""
            }
    }

    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        if (players.isEmpty()) return Pair(0.0, 0.0)

        val sortedCosts = players.map { it.transferCost }.sorted()
        val size = sortedCosts.size
        val median = if (size % 2 == 1) {
            sortedCosts[size / 2].toDouble()
        } else {
            (sortedCosts[size / 2 - 1] + sortedCosts[size / 2]).toDouble() / 2.0
        }

        val totalGoals = players.sumOf { it.goals }.toDouble()
        if (totalGoals == 0.0) return Pair(0.0, 0.0)

        val (lowOrEqualCostGroup, highCostGroup) = players.partition { it.transferCost <= median }

        val lowCostGoals = lowOrEqualCostGroup.sumOf { it.goals }.toDouble()
        val highCostGoals = highCostGroup.sumOf { it.goals }.toDouble()

        return Pair(lowCostGoals / totalGoals, highCostGoals / totalGoals)
    }

}