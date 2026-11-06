package chart

import model.Player
import model.toTeams
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.CategoryLabelPositions
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.labels.StandardPieSectionLabelGenerator
import org.jfree.chart.plot.DatasetRenderingOrder
import org.jfree.chart.plot.PiePlot
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.renderer.category.LineAndShapeRenderer
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.general.DefaultPieDataset
import java.awt.Color
import java.io.File
import java.text.NumberFormat

/** Задание 4, вариант 4 («Страны»). */
object Charts {

    /** Базовая: доля игроков из разных стран. */
    fun countryShare(players: List<Player>): JFreeChart {
        val dataset = DefaultPieDataset<String>()
        players.groupingBy { it.country }.eachCount()
            .entries.sortedByDescending { it.value }
            .forEach { dataset.setValue(it.key, it.value) }

        val chart = ChartFactory.createPieChart("Доля игроков по странам", dataset, true, true, false)
        val plot = chart.plot as PiePlot<*>
        plot.labelGenerator = StandardPieSectionLabelGenerator(
            "{0}: {1} ({2})", NumberFormat.getIntegerInstance(), NumberFormat.getPercentInstance()
        )
        return chart
    }

    /** Продвинутая: топ-10 команд по стоимости + накопленная доля (диаграмма Парето). */
    fun topTeamsPareto(players: List<Player>): JFreeChart {
        val totalAll = players.sumOf { it.transferCost }.toDouble()
        val top = players.toTeams()
            .sortedByDescending { it.totalTransferCost }
            .take(10)

        // накопление через runningFold; drop(1) убирает начальное значение 0
        val cumulativeShare = top.map { it.totalTransferCost }
            .runningFold(0L) { acc, cost -> acc + cost }
            .drop(1)
            .map { it / totalAll * 100 }

        val bars = DefaultCategoryDataset()
        top.forEach { bars.addValue(it.totalTransferCost / 1_000_000.0, "Суммарная стоимость, млн", it.name) }

        val line = DefaultCategoryDataset()
        top.zip(cumulativeShare).forEach { (team, share) -> line.addValue(share, "Накопленная доля, %", team.name) }

        val chart = ChartFactory.createBarChart(
            "Топ-10 команд по суммарной трансферной стоимости",
            "Команда", "Суммарная стоимость, млн",
            bars, PlotOrientation.VERTICAL, true, true, false
        )
        val plot = chart.categoryPlot
        plot.domainAxis.categoryLabelPositions = CategoryLabelPositions.UP_45

        plot.setDataset(1, line)
        plot.setRangeAxis(1, NumberAxis("Накопленная доля, %").apply { setRange(0.0, 100.0) })
        plot.mapDatasetToRangeAxis(1, 1)
        plot.setRenderer(1, LineAndShapeRenderer().apply {
            setSeriesPaint(0, Color.RED)
            setSeriesShapesVisible(0, true)
        })
        plot.datasetRenderingOrder = DatasetRenderingOrder.FORWARD
        return chart
    }

    fun save(chart: JFreeChart, file: File, width: Int = 1000, height: Int = 650) {
        file.parentFile?.mkdirs()
        ChartUtils.saveChartAsPNG(file, chart, width, height)
    }
}
