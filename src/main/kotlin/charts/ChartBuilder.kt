package charts

import model.Player
import model.Position
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartFrame
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import java.awt.Color
import java.io.File
import javax.swing.WindowConstants

object ChartBuilder {

    fun buildTopTeamsByCostChart(players: List<Player>): JFreeChart {
        val dataset = DefaultCategoryDataset()

        players
            .groupBy { it.team }
            .map { (team, teamPlayers) -> team.name to teamPlayers.sumOf { it.transferCost } }
            .sortedByDescending { it.second }
            .take(10)
            .forEach { (teamName, totalCost) ->
                dataset.addValue(totalCost.toDouble(), "Суммарная трансферная стоимость", teamName)
            }

        return ChartFactory.createBarChart(
            "Топ-10 команд по суммарной трансферной стоимости",
            "Команда",
            "Стоимость",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false
        )
    }

    fun buildForwardsCostGoalsChart(players: List<Player>): JFreeChart {
        val forwards = players
            .filter { it.position == Position.FORWARD }
            .sortedBy { it.transferCost }

        val lowCards = XYSeries("Жёлтые карточки: 0-2")
        val mediumCards = XYSeries("Жёлтые карточки: 3-5")
        val highCards = XYSeries("Жёлтые карточки: 6+")

        forwards.forEach { player ->
            val series = when {
                player.yellowCards <= 2 -> lowCards
                player.yellowCards <= 5 -> mediumCards
                else -> highCards
            }
            series.add(player.transferCost.toDouble(), player.goals.toDouble())
        }

        val scatterDataset = XYSeriesCollection().apply {
            addSeries(lowCards)
            addSeries(mediumCards)
            addSeries(highCards)
        }

        val chart = ChartFactory.createScatterPlot(
            "Трансферная стоимость и голы нападающих (цвет — жёлтые карточки)",
            "Трансферная стоимость",
            "Голы",
            scatterDataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        )

        val plot = chart.xyPlot
        val scatterRenderer = plot.renderer as XYLineAndShapeRenderer
        for (seriesIndex in 0..2) {
            scatterRenderer.setSeriesLinesVisible(seriesIndex, false)
        }
        scatterRenderer.setSeriesPaint(0, Color(76, 175, 80))
        scatterRenderer.setSeriesPaint(1, Color(255, 152, 0))
        scatterRenderer.setSeriesPaint(2, Color(211, 47, 47))

        val windowSize = 15
        if (forwards.size >= windowSize) {
            val trend = XYSeries("Тренд (скользящее среднее, окно $windowSize)")
            forwards.windowed(windowSize).forEach { window ->
                val avgCost = window.map { it.transferCost.toDouble() }.average()
                val avgGoals = window.map { it.goals.toDouble() }.average()
                trend.add(avgCost, avgGoals)
            }

            val trendDatasetIndex = 1
            plot.setDataset(trendDatasetIndex, XYSeriesCollection(trend))
            val trendRenderer = XYLineAndShapeRenderer(true, false)
            trendRenderer.setSeriesPaint(0, Color.BLACK)
            plot.setRenderer(trendDatasetIndex, trendRenderer)
        }

        return chart
    }

    fun saveChart(chart: JFreeChart, path: String, width: Int = 900, height: Int = 600) {
        val file = File(path)
        file.parentFile?.mkdirs()
        ChartUtils.saveChartAsPNG(file, chart, width, height)
    }

    fun showChart(chart: JFreeChart, title: String, width: Int = 900, height: Int = 600) {
        val frame = ChartFrame(title, chart)
        frame.pack()
        frame.setSize(width, height)
        frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
    }
}
