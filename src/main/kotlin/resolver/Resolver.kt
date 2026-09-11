package resolver

import model.Player
import model.Position
import model.Team

class Resolver(private val players: List<Player>) : IResolver {

    override fun getCountWithoutAgency(): Int = players.count { it.agency == null }

    override fun getBestScorerDefender(): Pair<String, Int> {
        return players
            .filter { it.position == Position.DEFENDER }
            .maxByOrNull { it.goals }
            ?.let { it.name to it.goals }
            ?: error("В списке не осталось ни одного защитника")
    }

    override fun getTheExpensiveGermanPlayerPosition(): String {
        return players
            .filter { it.nationality.equals("Germany", ignoreCase = true) }
            .maxByOrNull { it.transferCost }
            ?.position?.russianName
            ?: error("Ничего не найдено")
    }

    override fun getTheRudestTeam(): Team {
        return players
            .groupBy { it.team }
            .maxByOrNull { (team, teamPlayers) ->
                teamPlayers.sumOf { it.redCards }.toDouble() / team.numberOfPlayers
            }
            ?.key
            ?: error("Список игроков пуст")
    }

    override fun getAverageTransferCostByPosition(): Map<Position, Double> {
        return players
            .groupBy { it.position }
            .mapValues { (_, playerLis) ->
                playerLis.map { it.transferCost }.average()
            }
            .toList()
            .sortedByDescending{ it.second }
            .toMap()
    }

    override fun getMostValuablePlayers(): List<Player> {
        return players.
                sortedWith(compareByDescending<Player> { it.goals + it.assists * 2 }.thenBy { it.name } )
            .take(3)
    }

    override fun getMostPopularAgencyByCountry(): Map<String, String> {
        return players
            .groupBy { it.nationality }
            .mapValues { (_, playerList) ->
                playerList
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
        val sortedCosts = players.map { it.transferCost }.sorted()
        val size = sortedCosts.size
        val medianValue = if (size % 2 == 0) {
            (sortedCosts[size / 2 - 1] + sortedCosts[size / 2]) / 2.0
        } else {
            sortedCosts[size / 2].toDouble()
        }

        val sumOfGoals = players.sumOf { it.goals }.toDouble()
        val sumOfGoalsLess = players.filter { it.transferCost <= medianValue }.sumOf { it.goals }
        val sumOfGoalsMore = players.filter { it.transferCost > medianValue }.sumOf { it.goals }

        return (sumOfGoalsLess / sumOfGoals to sumOfGoalsMore / sumOfGoals)
    }
}