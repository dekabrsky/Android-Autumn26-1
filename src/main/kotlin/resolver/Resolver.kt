package resolver

import filters.PlayerFilter
import model.Player
import model.Position
import model.Team
import org.jetbrains.kotlinx.dataframe.api.median
import org.jetbrains.kotlinx.dataframe.api.toColumn
import kotlin.math.cos

class Resolver(private val players: List<Player>) : IResolver {

    override fun getCountWithoutAgency(): Int = players.count { it.agency == null }

    override fun getBestScorerDefender(): Pair<String, Int> {
        return players
            .filter { it.position == Position.DEFENDER }
            .maxByOrNull { it.goals }
            ?.let { it.name to it.goals }
            ?: error("В списке не осталось ни одного защитника")
    }

    override fun getBestScorerDefenderWithFilter(): Pair<String, Int> {
        val defaultFilter = PlayerFilter.ByPosition(Position.DEFENDER)

        return players
            .filter { defaultFilter.matches(it) }
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

        // Вручную находим медиану
        val sortedCosts = players.map { it.transferCost }.sorted()
        val size = sortedCosts.size
        val medianValue = if (size % 2 == 0) {
            (sortedCosts[size / 2 - 1] + sortedCosts[size / 2]) / 2.0
        } else {
            sortedCosts[size / 2].toDouble()
        }

        val sumOfGoals = players.sumOf { it.goals }.toDouble()
        val sumOfGoalsLess = players.filter { it.transferCost <= medianValue }.sumOf { it.goals }
        val sumOfGoalsMore = sumOfGoals - sumOfGoalsLess

        return (sumOfGoalsLess / sumOfGoals to sumOfGoalsMore / sumOfGoals)
    }

    override fun getGoalsShareByMedianCostWithFilter(): Pair<Double, Double> {

        // находим медиану с помощью библиотеки jetbrains.kotlinx:dataframe-core
        val costs = players.map { it.transferCost }
        val medianValue = costs.toColumn().median()

        val sumOfGoals = players.sumOf { it.goals }.toDouble()
        val cheaperFilter = PlayerFilter.CheaperThan(medianValue)
//        val expensiveFilter = PlayerFilter.Not(cheaperFilter)

        val sumLess = players.filter { cheaperFilter.matches(it) }.sumOf { it.goals }
        val sumMore = sumOfGoals - sumLess

        return (sumLess / sumOfGoals to sumMore / sumOfGoals)
    }
}