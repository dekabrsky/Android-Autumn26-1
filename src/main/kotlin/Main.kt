import parser.CsvParser
import resolver.Resolver
import grath.saveTopTeamsChart
import grath.saveStrikersTrendChart
import java.io.File

fun main() {
    System.setOut(java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8))

    val stream = CsvParser::class.java.getResourceAsStream("/fakePlayers.csv")!!
    val result = CsvParser.parse(stream)
    val resolver = Resolver(result.players)

    println("Игроков без агентства: ${resolver.getCountWithoutAgency()}")
    println("Лучший защитник и кол-во голов: ${resolver.getBestScorerDefender()}")
    println("Позиция самого дорогого немца: ${resolver.getTheExpensiveGermanPlayerPosition()}")
    println("Команда с наибольшим ср. числом красных карточек на игрока: ${resolver.getTheRudestTeam()}")
    println("Средняя трансферная стоимость по позициям: ${resolver.getAverageTransferCostByPosition()}")
    println("Топ-3 самых полезных игроков: ${resolver.getMostValuablePlayers()}")
    println("Самое популярное агентство по странам среди ее игроков: ${resolver.getMostPopularAgencyByCountry()}")
    println("Медианная трансферная стоимость: ${resolver.getGoalsShareByMedianCost()}")

    // 4 задание

    val outputDir = File("build/charts").apply { mkdirs() }

    saveTopTeamsChart(result.players, File(outputDir, "top_teams.png"), n = 10)
    println("\nБазовый график сохранён: ${File(outputDir, "top_teams.png").absolutePath}")

    saveStrikersTrendChart(result.players, File(outputDir, "strikers_trend.png"), window = 7)
    println("Продвинутый график сохранён: ${File(outputDir, "strikers_trend.png").absolutePath}")
}
