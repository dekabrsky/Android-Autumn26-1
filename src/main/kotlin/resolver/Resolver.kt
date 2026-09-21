package resolver

import model.Player
import model.Position
import model.Team
import java.text.NumberFormat

class Resolver(private val players: List<Player>) : IResolver {
    override fun getCountWithoutAgency(): Int {
        return players.count() { it.agency == null }
    }

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
            .filter { it.country == "Germany" }
            .maxByOrNull { it.transferCost }
            ?.position?.russianName ?: ""
    }

    override fun getTheRudestTeam(): Team {
        val result = players.groupBy { it.team }
            .map {
                (team, players) ->
                team to players.map { it.redCards }.average()
            }
            .maxByOrNull { it.second }

        if (result == null) {
            return Team("", "")
        }

        println("Самая гразно играющая команда: " + result.first)
        println("Среднее кол-во красных карт: " + result.second)
        return result.first
    }

    override fun getAverageTransferCostByPosition(): Map<Position, Double> {
        val result = players.groupBy { it.position }
            .mapValues { (_, posPlayers) ->
                posPlayers.map { it.transferCost }.average()
            }
            .toList()
            .sortedByDescending { it.second }
            .toMap()

        println("Средняя стоимость трансфера по позициям:")
        for (i in Position.entries) {
            println(i.name + ": " + if (result[i] != null)
            NumberFormat.getCurrencyInstance().format(result[i]) else "Нет данных")
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
        val result = players.groupBy { it.country }
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

        println("Самые популярные агенства по странам:")
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

        val goalsBelowOrEqualMedian =
            players.filter { it.transferCost <= median }.sumOf { it.goals }.toDouble()
        val goalsAboveMedian =
            players.filter { it.transferCost > median }.sumOf { it.goals }.toDouble()

        println(
            "Медианная стоимость трансфера: ${
                NumberFormat.getCurrencyInstance().format(median)
            }"
        )
        println(
            "Доля игроков дешевле медианы: " +
                    "${
                        NumberFormat.getPercentInstance()
                            .format(goalsBelowOrEqualMedian / totalGoals)
                    }"
        )

        println("Доля игроков дороже медианы:" +
                "${NumberFormat.getPercentInstance().format(goalsAboveMedian / totalGoals)}")

        return Pair(goalsBelowOrEqualMedian / totalGoals, goalsAboveMedian / totalGoals)

    }
}