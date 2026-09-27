import model.ParseResult
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import parser.CsvParser
import model.Player
import model.Position
import resolver.Resolver
import java.awt.Color
import java.io.File

fun main() {
    val parseResult = runParser()
    runAllResolvers(parseResult)
    val allPlayers = parseResult.players
    if (allPlayers.isEmpty()) return

    runBaseTask(allPlayers)
    runAdvancedTask(allPlayers)
}

fun runParser() : ParseResult {
    println("Запуск парсинга")
    val parseResult = CsvParser.parse()
    println("Успешно получено ${parseResult.players.size} игроков, пропущено ${parseResult.skippedCount} строк")
    return parseResult
}

fun runAllResolvers(parseResult: ParseResult) {
    println("Запуск всех запросов")
    val resolver = Resolver(parseResult.players)
    println("1. Количество игроков без агентства: ${resolver.getCountWithoutAgency()}")
    val bestPlayer = resolver.getBestScorerDefender()
    println("2. Лучший игрок: ${bestPlayer.first} - ${bestPlayer.second} голов")
    println("3. Самый дорогой немецкий игрок: ${resolver.getTheExpensiveGermanPlayerPosition()}")
    println("4. Самая грубая команда: ${resolver.getTheRudestTeam().name}")
    println("5. Средняя стоимость по позициям:")
    val averageCost = resolver.getAverageTransferCostByPosition()
    for (cost in averageCost)
        println("\t${cost.key.russianName} - ${String.format("%.2f миллионов", cost.value / 1_000_000.0)}")
    println("6. Самые ценные игроки:")
    val mvps = resolver.getMostValuablePlayers()
    for (mvp in mvps)
        println("\t${mvp.name} - ${mvp.goals + mvp.assists * 2} очков")
    println("7. Самые популярные агентства по странам:")
    val agencies = resolver.getMostPopularAgencyByCountry()
    for (agency in agencies)
        println("\t${agency.key}: ${agency.value}")
    val goalsShare = resolver.getGoalsShareByMedianCost()
    println(
        "8. Доли голов относительно медианы: " +
                "дешевле ${"%.1f".format(goalsShare.first * 100)}%, " +
                "дороже ${"%.1f".format(goalsShare.second * 100)}%"
    )
}

fun runBaseTask(allPlayers: List<Player>) {
    val top10Teams = allPlayers.groupBy { it.team }
        .entries
        .sortedByDescending { (_, players) -> players.sumOf { it.transferCost } }
        .take(10)

    val dataset = DefaultCategoryDataset()
    for ((team, players) in top10Teams) {
        val totalCost = players.sumOf { it.transferCost }
        val costInMillions = totalCost.toDouble() / 1_000_000.0
        dataset.addValue(costInMillions, "Стоимость состава в миллионах", team.name)
    }

    val chart = ChartFactory.createBarChart(
        "Топ-10 команд по суммарной трансферной стоимости",
        "Команда",
        "Стоимость в миллионах",
        dataset,
        PlotOrientation.VERTICAL,
        true,
        true,
        false
    )

    ChartUtils.saveChartAsPNG(File("teams_cost_top10.png"), chart, 1200, 800)
}

fun runAdvancedTask(allPlayers: List<Player>) {
    val forwards = allPlayers.filter { it.position == Position.FORWARD }.sortedBy { it.transferCost }
    if (forwards.isEmpty()) return

    val dataset = XYSeriesCollection()
    
    val s0 = XYSeries("0 ЖК")
    val s1 = XYSeries("1-3 ЖК")
    val s2 = XYSeries("4-6 ЖК")
    val s3 = XYSeries("7+ ЖК")
    val sTrend = XYSeries("Тренд")

    for (f in forwards) {
        val x = f.transferCost.toDouble() / 1_000_000.0
        val y = f.goals.toDouble()
        when {
            f.yellowCards == 0 -> s0.add(x, y)
            f.yellowCards in 1..3 -> s1.add(x, y)
            f.yellowCards in 4..6 -> s2.add(x, y)
            else -> s3.add(x, y)
        }
    }

    val windows = forwards.windowed(size = 10, step = 1)
    for (w in windows) {
        sTrend.add(w.map { it.transferCost }.average() / 1_000_000.0, w.map { it.goals }.average())
    }

    dataset.addSeries(s0)
    dataset.addSeries(s1)
    dataset.addSeries(s2)
    dataset.addSeries(s3)
    dataset.addSeries(sTrend)

    val chart = ChartFactory.createScatterPlot(
        "Связь стоимости и результативности нападающих",
        "Трансферная стоимость в миллионах",
        "Количество голов",
        dataset,
        PlotOrientation.VERTICAL,
        true,
        true,
        false
    )

    val plot = chart.plot as XYPlot
    val renderer = XYLineAndShapeRenderer()

    val colors = arrayOf(Color.GREEN, Color.BLUE, Color.MAGENTA, Color.RED)
    for (i in 0..3) {
        renderer.setSeriesLinesVisible(i, false)
        renderer.setSeriesShapesVisible(i, true)
        renderer.setSeriesPaint(i, colors[i])
    }

    renderer.setSeriesLinesVisible(4, true)
    renderer.setSeriesShapesVisible(4, false)
    renderer.setSeriesPaint(4, Color.BLACK)
    
    plot.renderer = renderer
    ChartUtils.saveChartAsPNG(File("forwards_trend.png"), chart, 1200, 800)
}
