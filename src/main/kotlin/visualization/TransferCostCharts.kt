package visualization

import model.Player
import model.Position
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset
import org.jfree.data.statistics.HistogramDataset
import java.awt.Color
import java.awt.Font
import java.io.File

object TransferCostCharts {
    private const val WIDTH = 1100
    private const val HEIGHT = 720
    private const val HISTOGRAM_BINS = 12

    fun generateAll(
        players: List<Player>,
        outputDir: File = File("build/reports/football"),
    ): List<File> {
        require(players.isNotEmpty()) { "Cannot build charts without players" }

        outputDir.mkdirs()
        return listOf(
            saveDistributionChart(players, File(outputDir, "transfer_cost_distribution.png")),
            savePositionBoxPlot(players, File(outputDir, "transfer_cost_by_position.png")),
        )
    }

    fun saveDistributionChart(players: List<Player>, outputFile: File): File {
        val dataset = HistogramDataset().apply {
            addSeries(
                "Игроки",
                players.map { it.transferCost.toDouble() }.toDoubleArray(),
                HISTOGRAM_BINS,
            )
        }

        val chart = ChartFactory.createHistogram(
            "Распределение трансферных стоимостей",
            "Трансферная стоимость",
            "Количество игроков",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false,
        ).applyCommonStyle()

        outputFile.parentFile?.mkdirs()
        ChartUtils.saveChartAsPNG(outputFile, chart, WIDTH, HEIGHT)
        return outputFile
    }

    fun savePositionBoxPlot(players: List<Player>, outputFile: File): File {
        val dataset = DefaultBoxAndWhiskerCategoryDataset()

        Position.entries.forEach { position ->
            val costs = players
                .filter { it.position == position }
                .mapTo(mutableListOf<Number>()) { it.transferCost }

            if (costs.isNotEmpty()) {
                dataset.add(costs, "Стоимость", position.russianName)
            }
        }

        val chart = ChartFactory.createBoxAndWhiskerChart(
            "Разброс трансферных стоимостей по позициям",
            "Позиция",
            "Трансферная стоимость",
            dataset,
            false,
        ).applyCommonStyle()

        outputFile.parentFile?.mkdirs()
        ChartUtils.saveChartAsPNG(outputFile, chart, WIDTH, HEIGHT)
        return outputFile
    }

    private fun JFreeChart.applyCommonStyle(): JFreeChart {
        backgroundPaint = Color.WHITE
        title.font = Font(Font.SANS_SERIF, Font.BOLD, 24)
        plot.backgroundPaint = Color(248, 249, 250)
        plot.outlinePaint = Color(210, 214, 220)
        return this
    }
}
