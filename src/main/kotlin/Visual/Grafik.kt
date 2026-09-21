package Visual

import model.Player
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.CategoryAxis
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.CategoryPlot
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.renderer.category.BarRenderer
import org.jfree.chart.renderer.category.LineAndShapeRenderer
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.general.DefaultPieDataset
import java.awt.Color
import java.io.File
import java.text.NumberFormat

fun plotCountryShares(players: List<Player>, outputPath: String = "country_shares.png") {
    if (players.isEmpty()) {
        println("Нет данных для построения диаграммы по странам.")
        return
    }

    val countryCounts = players.groupingBy { it.country }.eachCount()
    val dataset = DefaultPieDataset<String>()
    countryCounts.forEach { (country, count) -> dataset.setValue(country, count) }

    val chart = ChartFactory.createPieChart(
        "Доля игроков по странам",
        dataset,
        true,   // legend
        true,   // tooltips
        false   // URLs
    )

    ChartUtils.saveChartAsPNG(File(outputPath), chart, 800, 600)
    println("Диаграмма долей стран сохранена в $outputPath")
}

fun plotTopTeamsCumulativeCost(
    players: List<Player>,
    outputPath: String = "top_teams_cumulative.png"
) {
    if (players.isEmpty()) {
        println("Нет данных для построения диаграммы по командам.")
        return
    }

    // Группируем по команде и суммируем стоимость
    val teamCosts = players
        .groupBy { it.team }
        .mapValues { (_, teamPlayers) -> teamPlayers.sumOf { it.transferCost } }
        .toList()
        .sortedByDescending { it.second }
        .take(10)

    if (teamCosts.isEmpty()) {
        println("Не удалось выделить топ-10 команд.")
        return
    }

    val totalCost = teamCosts.sumOf { it.second }
    var cumulative = 0L
    val cumulativeShares = teamCosts.map { (_, cost) ->
        cumulative += cost
        cumulative.toDouble() / totalCost
    }

    // Данные для столбцов
    val barDataset = DefaultCategoryDataset()
    teamCosts.forEach { (team, cost) ->
        barDataset.addValue(cost, "Суммарная стоимость", team.name)
    }

    // Данные для линии накопленной доли
    val lineDataset = DefaultCategoryDataset()
    teamCosts.forEachIndexed { index, (team, _) ->
        lineDataset.addValue(cumulativeShares[index], "Накопленная доля", team.name)
    }

    // Настройка осей
    val categoryAxis = CategoryAxis("Команда")
    val valueAxis1 = NumberAxis("Суммарная стоимость")
    val valueAxis2 = NumberAxis("Накопленная доля")
    valueAxis2.setRange(0.0, 1.0)
    valueAxis2.numberFormatOverride = NumberFormat.getPercentInstance()

    // Рендереры
    val barRenderer = BarRenderer()
    val lineRenderer = LineAndShapeRenderer(true, false)
    lineRenderer.setSeriesPaint(0, Color.RED)

    // Создаём комбинированный график
    val plot = CategoryPlot(barDataset, categoryAxis, valueAxis1, barRenderer)
    plot.setDataset(1, lineDataset)
    plot.setRenderer(1, lineRenderer)
    plot.setRangeAxis(1, valueAxis2)
    plot.mapDatasetToRangeAxis(1, 1)

    val chart = JFreeChart(
        "Топ-10 команд по суммарной трансферной стоимости",
        JFreeChart.DEFAULT_TITLE_FONT,
        plot,
        true
    )

    ChartUtils.saveChartAsPNG(File(outputPath), chart, 1000, 600)
    println("Комбинированная диаграмма по командам сохранена в $outputPath")
}