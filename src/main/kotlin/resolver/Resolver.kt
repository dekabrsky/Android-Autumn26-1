package resolver

import model.Player
import model.Position
import model.Team

class Resolver(private val players: List<Player>) : IResolver {

    override fun getCountWithoutAgency(): Int {
        return players.count { it.agency == null }
    }

    override fun getBestScorerDefender(): Pair<String, Int> {
        val defender = players
            .filter { it.position == Position.DEFENDER }
            .maxByOrNull { it.goals }
        return Pair(defender?.name ?: "", defender?.goals ?: 0)
    }

    override fun getTheExpensiveGermanPlayerPosition(): String {
        val player = players
            .filter { it.nationality.equals("Germany", ignoreCase = true) }
            .maxByOrNull { it.transferCost }
        return player?.position?.russianName ?: "не найдено"
    }

    override fun getTheRudestTeam(): Team {
        val team = players
            .groupBy { it.team }
            .mapValues { (_, teamPlayers) ->
                teamPlayers.sumOf { it.redCards }.toDouble() / teamPlayers.size
            }
            .maxByOrNull { it.value }
            ?.key
        return team ?: Team("не найдено", "")
    }

    override fun getAverageTransferCostByPosition(): Map<Position, Double> =
        players.groupBy { it.position }
            .mapValues { (_, list) -> list.map { it.transferCost }.average() }
            .entries
            .sortedByDescending { it.value }
            .associate { it.key to it.value }

    override fun getMostValuablePlayers(): List<Player> {
        return players
            .map { player ->
                val utility = player.goals + 2 * player.assists
                player to utility
            }
            .sortedWith(
                compareByDescending<Pair<Player, Int>> { it.second }
                    .thenBy { it.first.name }
            )
            .take(3)
            .map { it.first }
    }

    override fun getMostPopularAgencyByCountry(): Map<String, String> {
        return players
            .filter { it.agency != null }
            .groupBy { it.nationality }
            .mapValues { (_, countryPlayers) ->
                countryPlayers
                    .groupBy { it.agency }
                    .mapValues { (_, group) -> group.size }
                    .maxByOrNull { it.value }
                    ?.key
                    ?: "не найдено"
            }
            .toMap()
    }

    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        if (players.isEmpty()) return 0.0 to 0.0

        val sortedCosts = players.map { it.transferCost }.sorted()
        val median = if (sortedCosts.size % 2 == 0) {
            (sortedCosts[sortedCosts.size / 2 - 1] + sortedCosts[sortedCosts.size / 2]) / 2.0
        } else {
            sortedCosts[sortedCosts.size / 2].toDouble()
        }

        val totalGoals = players.sumOf { it.goals }.toDouble()
        if (totalGoals == 0.0) return 0.0 to 0.0

        val goalsBelowOrEqualMedian = players
            .filter { it.transferCost <= median }
            .sumOf { it.goals }
            .toDouble()

        val goalsAboveMedian = players
            .filter { it.transferCost > median }
            .sumOf { it.goals }
            .toDouble()

        return goalsBelowOrEqualMedian / totalGoals to goalsAboveMedian / totalGoals
    }
}