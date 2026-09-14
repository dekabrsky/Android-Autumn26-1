package resolver

import model.Player
import model.Position
import model.Team

class Resolver(val players: List<Player>): IResolver{
    override fun getCountWithoutAgency(): Int {
        return players.count { it.Agency == null }
    }

    override fun getBestScorerDefender(): Pair<String, Int> {
        return players.asSequence()
            .filter { it.Position == Position.DEFENDER }
            .maxBy { it.Goals }
            .let { it.Name to it.Goals }
    }

    override fun getTheExpensiveGermanPlayerPosition(): String {
        return players.asSequence()
            .filter { it.Nationality.equals("Germany", ignoreCase = true) }
            .maxBy { it.TransferCost }
            .Position.name
    }

    override fun getTheRudestTeam(): Team {
        return players
            .groupBy { it.Team }
            .maxBy { (_, teamPlayers) -> teamPlayers.map { it.Red_cards }.average() }
            .key

    }

    override fun getAverageTransferCostByPosition(): Map<Position, Double> {
        return players.groupBy { it.Position }
            .map { (pos, list) -> pos to list.sumOf { it.TransferCost }.toDouble() / list.size }
            .sortedByDescending { it.second }
            .toMap()
    }

    override fun getMostValuablePlayers(): List<Player> {
        return players
            .sortedWith(compareByDescending<Player> { it.Goals + 2 * it.Assists }.thenBy { it.Name })
            .take(3)
    }

    override fun getMostPopularAgencyByCountry(): Map<String, String> {
        return players
            .filter { it.Agency != null }
            .groupBy { it.Nationality }
            .mapValues { (_, countryPlayers) ->
                countryPlayers
                    .groupingBy { it.Agency!! }
                    .eachCount()
                    .entries
                    .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
                    .first()
                    .key
            }
    }

    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        if (players.isEmpty()) return 0.0 to 0.0

        val costs = players.map { it.TransferCost }.sorted()
        val middle = costs.size / 2
        val median = if (costs.size % 2 == 0) {
            (costs[middle - 1] + costs[middle]) / 2.0
        } else {
            costs[middle].toDouble()
        }

        val totalGoals = players.sumOf { it.Goals }
        if (totalGoals == 0) return 0.0 to 0.0

        val cheaperGoals = players.filter { it.TransferCost <= median }.sumOf { it.Goals }
        val expensiveGoals = totalGoals - cheaperGoals

        return cheaperGoals.toDouble() / totalGoals to expensiveGoals.toDouble() / totalGoals
    }

}