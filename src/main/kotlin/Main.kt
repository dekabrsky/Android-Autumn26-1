import parser.CsvParser
import report.plotCountryProportions
import resolver.Resolver
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.streams.asSequence

fun task1Helper(path: Path, name: String) {
    println(name)
    val csvLines = Files.lines(path).asSequence()
    val p = CsvParser
    val result = p.parse(csvLines)
    println("Распознано игроков: " + result.players.count())
    println("Пропущено игроков: " + result.skippedCount)
}

fun task1() {
    println("=== Задание 1 ===")
    val dirtyFilePath = Path("src/main/resources/fakePlayersDirty.csv")
    val cleanFilePath = Path("src/main/resources/fakePlayers.csv")

    task1Helper(cleanFilePath, "Чистый файл")
    println()
    task1Helper(dirtyFilePath, "Грязный файл")

    println("=================")
}

fun task2() {
    println("=== Задание 2 ===")
    val path = Path("src/main/resources/fakePlayersDirty.csv")
    val csvLines = Files.lines(path).asSequence()
    val p = CsvParser
    val result = p.parse(csvLines)

    val resolver = Resolver(result.players)

    println("Игроков без агентства: " + resolver.getCountWithoutAgency())
    println()
    println("Лучший бомбардир среди защитников: " + resolver.getBestScorerDefender())
    println()
    println("Позиция самого дорого немецкого игрока: " + resolver.getTheExpensiveGermanPlayerPosition())
    println()
    resolver.getTheRudestTeam()
    println()
    resolver.getAverageTransferCostByPosition()
    println()
    resolver.getMostValuablePlayers()
    println()
    resolver.getMostPopularAgencyByCountry()
    println()
    resolver.getGoalsShareByMedianCost()
    println()
    println("=================")
}

fun task4() {
    println("=== Задание 4 ===")
    val path = Path("src/main/resources/fakePlayersDirty.csv")
    val csvLines = Files.lines(path).asSequence()
    val p = CsvParser
    val result = p.parse(csvLines)
    plotCountryProportions(result.players)
    println("=================")
}

fun main(args: Array<String>) {
    task1()
    task2()
    task4()
}