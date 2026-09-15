package visualization

import model.Player
import model.Team
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartFrame
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset
import org.jfree.data.statistics.HistogramDataset
import java.io.File

object PlayerVisualization {

    fun transferCostChart(players: List<Player>): JFreeChart {
        val dataset = HistogramDataset()

        if (players.isNotEmpty()) {
            val values = players.map { it.transferCost / 1_000_000.0 }.toDoubleArray()
            dataset.addSeries("Игроки", values, 10)
        }

        return ChartFactory.createHistogram(
            "Распределение трансферной стоимости игроков",
            "Трансферная стоимость (млн €)",
            "Количество игроков",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false
        )
    }

    fun transferDispersionChart(teams: List<Team>): JFreeChart {
        val allPlayers = teams.flatMap { it.players }
        return createPositionBoxPlot(allPlayers)
    }

    private fun createPositionBoxPlot(players: List<Player>): JFreeChart {
        val dataset = DefaultBoxAndWhiskerCategoryDataset()

        players.groupBy { it.position }.forEach { (position, playerList) ->
            val values = playerList.map { it.transferCost / 1_000_000.0 }
            if (values.isNotEmpty()) {
                dataset.add(values, "Стоимость", position.russianName)
            }
        }

        val chart = ChartFactory.createBoxAndWhiskerChart(
            "Разброс трансферных стоимостей по позициям",
            "Позиция",
            "Трансферная стоимость (млн €)",
            dataset,
            false
        )

        val plot = chart.categoryPlot
        plot.backgroundPaint = java.awt.Color.WHITE
        plot.domainGridlinePaint = java.awt.Color.LIGHT_GRAY
        plot.rangeGridlinePaint = java.awt.Color.LIGHT_GRAY

        plot.renderer = org.jfree.chart.renderer.category.BoxAndWhiskerRenderer().apply {
            isMeanVisible = false
            isMedianVisible = true
            fillBox = true
            itemMargin = 0.2
        }

        return chart
    }

    fun save(chart: JFreeChart, path: String, width: Int = 800, height: Int = 600) {
        val file = File(path)
        file.parentFile?.mkdirs()
        ChartUtils.saveChartAsPNG(file, chart, width, height)
    }

    fun display(chart: JFreeChart, title: String) {
        val frame = ChartFrame(title, chart)
        frame.pack()
        frame.isVisible = true
    }
}