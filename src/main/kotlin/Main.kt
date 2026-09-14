import model.Position
import model.Player
import parser.CsvParser
import resolver.Resolver
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.statistics.BoxAndWhiskerCalculator
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset
import org.jfree.data.statistics.HistogramDataset
import java.io.File
import kotlin.collections.map

fun main() {

    System.setOut(PrintStream(System.out, true, StandardCharsets.UTF_8))
    val parser = CsvParser()
    val result = parser.parse("src/main/resources/fakePlayersDirty.csv")

    println("Всего игроков: ${result.players.size}")
    println("Пропущено строк: ${result.skippedCount}")

    if (result.players.isEmpty()) {
        println("Нет данных для анализа")
        return
    }

    val resolver = Resolver(result.players)

    println("\n")

    println("\n1. Игроков без агентства: ${resolver.getCountWithoutAgency()}")

    val bestScorer = resolver.getBestScorerDefender()
    println("\n2. Лучший защитник-бомбардир: ${bestScorer.first} (${bestScorer.second} голов)")

    println("\n3. Позиция самого дорогого немца: ${resolver.getTheExpensiveGermanPlayerPosition()}")

    val rudestTeam = resolver.getTheRudestTeam()
    println("\n4. Команда с наибольшим средним красных: ${rudestTeam.name} (${rudestTeam.city})")

    println("\n5. Средняя стоимость по позициям (по убыванию):")
    resolver.getAverageTransferCostByPosition()
        .toList()
        .sortedByDescending { it.second }
        .forEach { (position, avg) ->
            println("   ${position.russianName.padEnd(15)} ${"%.2f".format(avg)}")
        }

    println("\n6. Топ-3 самых полезных игроков:")
    resolver.getMostValuablePlayers().forEachIndexed { index, player ->
        val utility = player.goals + 2 * player.assists
        println("   ${index + 1}. ${player.name} (полезность: $utility)")
    }

    println("\n7. Самое популярное агентство по странам:")
    resolver.getMostPopularAgencyByCountry().forEach { (country, agency) ->
        println("   $country - $agency")
    }

    val (below, above) = resolver.getGoalsShareByMedianCost()
    println("\n8. Распределение голов относительно медианной стоимости:")
    println("   Игроки не дороже медианы: ${"%.1f".format(below * 100)}% голов")
    println("   Игроки дороже медианы: ${"%.1f".format(above * 100)}% голов")
    println("\n")

    println("Задание 4. Вариант 3. Визуализация")
    plotTransferCostDistribution(result.players)
    println("Сохранена гистограмма распределения трансферных стоимостей: transfer_cost_distribution.png")
    plotTransferCostByPosition(result.players)
    println("Сохранен box plot трансферных стоимостей по позициям: transfer_cost_by_position.png")
}

fun plotTransferCostDistribution(players: List<Player>) {
    val dataset = HistogramDataset()
    val costs = players.map { it.transferCost.toDouble() }.toDoubleArray()
    dataset.addSeries("Трансферная стоимость", costs, 30)

    val chart = ChartFactory.createHistogram(
        "Распределение трансферных стоимостей всех игроков",
        "Трансферная стоимость",
        "Количество игроков",
        dataset,
        PlotOrientation.VERTICAL,
        false,
        true,
        false
    )

    ChartUtils.saveChartAsPNG(File("transfer_cost_distribution.png"), chart, 800, 600)
}

fun plotTransferCostByPosition(players: List<Player>) {
    val dataset = DefaultBoxAndWhiskerCategoryDataset()

    Position.entries.forEach { position ->
        val costs = players
            .filter { it.position == position }
            .map { it.transferCost.toDouble() }
        if (costs.isNotEmpty()) {
            val boxData = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(costs)
            dataset.add(boxData, position.russianName, "Стоимость")
        }
    }

    val chart = ChartFactory.createBoxAndWhiskerChart(
        "Разброс трансферных стоимостей по позициям",
        "Позиция",
        "Трансферная стоимость",
        dataset,
        true
    )

    ChartUtils.saveChartAsPNG(File("transfer_cost_by_position.png"), chart, 800, 600)
}