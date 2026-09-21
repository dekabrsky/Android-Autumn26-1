package Denisova1dz

import Denisova1dz.parser.CsvParser
import java.io.File

fun main() {
    val result = CsvParser.parse("src/main/resources/fakePlayersDirty.csv")

    println("Успешно прочли игроков: ${result.players.size}")
    println("Пропустили невалидных строк: ${result.skippedCount}")

    val file = File("src/main/resources/fakePlayers.csv")
    val parseResult = CsvParser.parse("src/main/resources/fakePlayers.csv")
    val resolver = Denisova1dz.resolver.Resolver(parseResult.players)

    println("1. Без агентства: ${resolver.getCountWithoutAgency()}")
    println("2. Лучший защитник-бомбардир: ${resolver.getBestScorerDefender()}")
    println("3. Позиция самого дорогого немца: ${resolver.getTheExpensiveGermanPlayerPosition()}")
    println("4. Больше всего красных карточек у команды: ${resolver.getTheRudestTeam()}")
    println("5. Средняя стоимость по позициям: ${resolver.getAverageTransferCostByPosition()}")
    println("6. Топ-3 полезных игрока: ${resolver.getMostValuablePlayers().map { it.name }}")
    println("7. Популярные агентства по странам: ${resolver.getMostPopularAgencyByCountry()}")
    println("8. Доли голов по медиане стоимости: ${resolver.getGoalsShareByMedianCost()}")

    Task4Charts.showBasicChart(parseResult.players)
    Task4Charts.showAdvancedChart(parseResult.players)
}