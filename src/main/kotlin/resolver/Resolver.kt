package resolver

import model.Player
import model.Position
import model.Team

class Resolver(private val players: List<Player>): IResolver {
    override fun getCountWithoutAgency(): Int =
        players.count { it.agency == null }


    override fun getBestScorerDefender(): Pair<String, Int> {
        val bestDefender = players
            .filter { it.position == Position.DEFENDER }
            .maxByOrNull { it.goals }
        return Pair(bestDefender?.name ?: "", bestDefender?.goals ?: 0)
    }

    override fun getTheExpensiveGermanPlayerPosition(): String {
        val mostExpensive = players
            .filter { it.country == "Germany" }
            .maxByOrNull { it.transferCost }
        return mostExpensive?.position?.russianName ?: ""
    }

    override fun getTheRudestTeam(): Team {
        val teamWithMaxAvgRed = players
            .groupBy { it.team }
            .maxByOrNull { (_, teamPlayers) ->
            teamPlayers.map { it.redCards }.average()
        }
        return teamWithMaxAvgRed?.key ?: Team("", "")
    }

    override fun getAverageTransferCostByPosition(): Map<Position, Double> {
        return players
            .groupBy { it.position }
            .mapValues { (_, positionPlayers) ->
                positionPlayers.map { it.transferCost }.average()
            }
            .toList()
            .sortedByDescending { (_, avgCost) -> avgCost }
            .toMap()
    }

    override fun getMostValuablePlayers(): List<Player> {
        return players
            .sortedWith(
                compareByDescending<Player> {it.goals + 2*it.assists}
                    .thenBy { it.name }
            )
            .take(3)
    }

    override fun getMostPopularAgencyByCountry(): Map<String, String> {
        return players
            .filter { it.agency != null }
            .groupBy { it.country }
            .mapValues { (_, countryPlayers) ->
                countryPlayers.groupBy { it.agency }
                    .maxWithOrNull(
                        compareBy<Map.Entry<String?, List<Player>>> { it.value.size }
                            .thenByDescending { it.key ?: "" }
                    )
                    ?.key ?: ""
            }
            .filterValues { it.isNotEmpty() }
    }

    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        if (players.isEmpty()) return Pair(0.0, 0.0)
        val totalGoals = players.sumOf { it.goals }
        if (totalGoals == 0) return Pair(0.0, 0.0)

        val sortedCosts = players.map { it.transferCost }.sorted()
        val median = (sortedCosts[(sortedCosts.size - 1) / 2] + sortedCosts[sortedCosts.size / 2]) / 2.0

        val (below, above) = players.partition { it.transferCost <= median }
        return below.sumOf { it.goals }.toDouble() / totalGoals to
                above.sumOf { it.goals }.toDouble() / totalGoals
    }
}