package charts

import model.Player
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.plot.PiePlot
import org.jfree.data.general.DefaultPieDataset
import java.awt.Color
import java.awt.Font
import java.io.File


fun buildCountryShareChart(players: List<Player>) {
    val counts = players
        .groupingBy { it.country }
        .eachCount()
        .entries
        .sortedByDescending { it.value }


    val dataset = DefaultPieDataset<String>()
    counts.forEach { (country, count) -> dataset.setValue(country, count) }


    val chart = ChartFactory.createPieChart(
        "Доля игроков из разных стран",
        dataset,
        true,
        true,
        false
    )


    val plot = chart.plot as PiePlot<String>
    plot.labelFont = Font("SansSerif", Font.PLAIN, 12)
    plot.labelPaint = Color.BLACK
    plot.labelBackgroundPaint = Color(255, 255, 255, 200)
    plot.isCircular = true
    plot.interiorGap = 0.02


    val file = File("country_share.png")
    ChartUtils.saveChartAsPNG(file, chart, 1200, 900)
    println("Диаграмма сохранена: ${file.absolutePath}")


    val total = players.size
    println()
    println("Доля игроков по странам (всего $total):")
    counts.forEach { (country, count) ->
        val percent = count * 100.0 / total
        println("  %-40s %3d  (%.2f%%)".format(country, count, percent))
    }
}