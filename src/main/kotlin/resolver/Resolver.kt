package resolver

import model.Player
import model.Position
import model.Team
import model.TeamSquad
import java.text.NumberFormat

class Resolver(private val players: List<Player>) : IResolver {

    private val teamSquads: List<TeamSquad> by lazy { TeamSquad.fromPlayers(players) }

    override fun getCountWithoutAgency(): Int =
        players.count { it.agency == null }

    override fun getBestScorerDefender(): Pair<String, Int> {
        val bestDefender = players
            .filter { it.position == Position.DEFENDER }
            .maxByOrNull { it.goals }
        return if (bestDefender != null) {
            Pair(bestDefender.name, bestDefender.goals)
        } else {
            Pair("", 0)
        }
    }

    override fun getTheExpensiveGermanPlayerPosition(): String {
        return players
            .filter { it.nationality == "Germany" }
            .maxByOrNull { it.transferCost }
            ?.position?.russianName ?: ""
    }

    override fun getTheRudestTeam(): Team {
        val result = teamSquads.maxByOrNull { it.averageRedCards }
        if (result == null) {
            return Team("", "")
        }
        println("Самая грязно играющая команда: " + result.team)
        println("Среднее кол-во красных карт: " + result.averageRedCards)
        return result.team
    }

    override fun getAverageTransferCostByPosition(): Map<Position, Double> {
        val result = players.groupBy { it.position }
            .mapValues { (_, posPlayers) ->
                posPlayers.map { it.transferCost }.average()
            }
            .toList()
            .sortedByDescending { it.second }
            .toMap()

        println("Средняя стоимость транфера по позициям:")
        for (i in Position.entries) {
            println(i.name + ": " + if (result[i] != null) NumberFormat.getCurrencyInstance().format(result[i]) else "Нет данных")
        }

        return result
    }

    override fun getMostValuablePlayers(): List<Player> {
        val result = players.sortedWith(
            compareByDescending<Player> { it.goals + 2 * it.assists }
                .thenBy { it.name }
        ).take(3)
        println("Топ 3 игрока:")
        println(result[0])
        println(result[1])
        println(result[2])
        return result
    }

    override fun getMostPopularAgencyByCountry(): Map<String, String> {
        val result = players.groupBy { it.nationality }
            .mapValues { (_, countryPlayers) ->
                countryPlayers.mapNotNull { it.agency }
                    .groupingBy { it }
                    .eachCount()
                    .toList()
                    .sortedWith(
                        compareByDescending<Pair<String, Int>> { it.second }
                            .thenBy { it.first }
                    )
                    .firstOrNull()?.first ?: ""
            }
        println("Самые популярные агентства по странам:")
        for (i in result.keys) {
            println(i + ": " + result[i])
        }
        return result
    }

    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        if (players.isEmpty()) return Pair(0.0, 0.0)

        val sortedCosts = players.map { it.transferCost }.sorted()
        val median = if (sortedCosts.size % 2 == 0) {
            (sortedCosts[sortedCosts.size / 2 - 1] + sortedCosts[sortedCosts.size / 2]) / 2.0
        } else {
            sortedCosts[sortedCosts.size / 2].toDouble()
        }

        val totalGoals = players.sumOf { it.goals }.toDouble()
        if (totalGoals == 0.0) return Pair(0.0, 0.0)

        val goalsBelowOrEqualMedian = players.filter { it.transferCost <= median }.sumOf { it.goals }.toDouble()
        val goalsAboveMedian = players.filter { it.transferCost > median }.sumOf { it.goals }.toDouble()

        println("Медианная стоимость трансфера: ${NumberFormat.getCurrencyInstance().format(median)}")
        println("Доля игроков дешевле медианы: ${NumberFormat.getPercentInstance().format(goalsBelowOrEqualMedian / totalGoals)}")
        println("Доля игроков дороже медианы: ${NumberFormat.getPercentInstance().format(goalsAboveMedian / totalGoals)}")

        return Pair(goalsBelowOrEqualMedian / totalGoals, goalsAboveMedian / totalGoals)
    }
}
