import charts.Charts
import parser.CsvParser
import resolver.Resolver
import java.io.File

fun main() {
    val lines = File("src/main/resources/fakePlayersDirty.csv")
        .readLines()

    val parseResult = CsvParser.parse(lines)
    val resolver = Resolver(parseResult.players)

    println("Игроков без агентства: ${resolver.getCountWithoutAgency()}")
    println("Лучший защитник: ${resolver.getBestScorerDefender()}")
    println("Позиция самого дорогого немца: ${resolver.getTheExpensiveGermanPlayerPosition()}")
    println("Самая грубая команда: ${resolver.getTheRudestTeam()}")

    println("Средняя стоимость по позициям:")
    println(resolver.getAverageTransferCostByPosition())

    println("Топ-3 полезных игроков:")
    resolver.getMostValuablePlayers().forEach { println(it) }

    println("Популярные агентства по странам:")
    println(resolver.getMostPopularAgencyByCountry())

    println("Доли голов по медиане:")
    println(resolver.getGoalsShareByMedianCost())

    Charts.showTransferCostDistribution(parseResult.players)
    Charts.showTransferCostSpreadByPosition(parseResult.players)
}