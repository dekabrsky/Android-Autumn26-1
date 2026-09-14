import parser.readCSV
import model.Player
import resolver.Resolver

import java.util.Locale

fun main() {
    val inputStream = object {}.javaClass.getResourceAsStream("/fakePlayersDirty.csv")

    val result = readCSV(inputStream)
    val players = result.players
    val resolver = Resolver(players)
    val rudestTeam = resolver.getTheRudestTeam()

    println("1) Загружено игроков: ${result.players.size}, Пропущено строк: ${result.skippedCount}")

    println("2.1) Игорьков без агентства: ${resolver.getCountWithoutAgency()}")
    println("2.2) Наибольшее число голов среди защитников: ${resolver.getBestScorerDefender()}")
    println("2.3) Самая дорогая позиция немецкого грока: ${resolver.getTheExpensiveGermanPlayerPosition()}")
    println("2.4) Команда с наибольшим средним числом красных карточек: ${rudestTeam.name}, город ${rudestTeam.city}")
    println("2.5) Средняя трансферная стоимость по позициям: ")
    resolver.getAverageTransferCostByPosition()
        .forEach { (position, averageCost) ->
            println(
                "${position.className}: ${"%,.2f".format(averageCost)}"
            )
        }

    println("2.6) Топ-3 самых полезных игрока:")
    resolver.getMostValuablePlayers()
        .forEachIndexed { index, player ->
            val usefulness = player.goals + 2 * player.assists

            println(
                "${index + 1}. ${player.name} - полезность: $usefulness"
            )
        }

    println("2.7) Самое популярное агентство по странам:")
    resolver.getMostPopularAgencyByCountry()
        .forEach { (country, agency) ->
            println("$country — $agency")
        }

    val shares = resolver.getGoalsShareByMedianCost()
    println("2.8) Доли голов относительно медианной стоимости:")
    println("Не дороже медианы: ${"%.2f".format(shares.first * 100)}%")
    println("Дороже медианы: ${"%.2f".format(shares.second * 100)}%")
}