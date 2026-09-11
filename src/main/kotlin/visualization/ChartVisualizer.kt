package visualization

import java.awt.Color
import java.awt.Font
import java.nio.file.Files
import java.nio.file.Path
import model.Player
import model.Position
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.axis.NumberTickUnit
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer
import org.jfree.chart.renderer.xy.XYBarRenderer
import org.jfree.chart.title.TextTitle
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset
import org.jfree.data.statistics.HistogramDataset
import org.jfree.data.statistics.HistogramType

data class Charts(
    val costDistribution: Path,
    val costByPosition: Path,
)

object ChartVisualizer {
    private const val MILLION = 1_000_000.0

    fun createCharts(players: List<Player>, outputDirectory: Path): Charts {
        require(players.isNotEmpty()) { "Cannot build charts for an empty player list" }
        Files.createDirectories(outputDirectory)

        val distributionFile = outputDirectory.resolve("transfer-cost-distribution.png")
        val byPositionFile = outputDirectory.resolve("transfer-cost-by-position.png")

        ChartUtils.saveChartAsPNG(distributionFile.toFile(), distributionChart(players), 1_200, 720)
        ChartUtils.saveChartAsPNG(byPositionFile.toFile(), byPositionChart(players), 1_200, 720)

        return Charts(distributionFile, byPositionFile)
    }

    private fun distributionChart(players: List<Player>) =
        HistogramDataset().also { dataset ->
            dataset.type = HistogramType.FREQUENCY
            val costsInMillions = players.map { it.transferCost / MILLION }.toDoubleArray()
            val maximumCost = costsInMillions.max()
            val upperBound = kotlin.math.ceil(maximumCost / 10.0) * 10.0
            val bins = (upperBound / 10.0).toInt()
            dataset.addSeries("Игроки", costsInMillions, bins, 0.0, upperBound)
        }.let { dataset ->
            ChartFactory.createHistogram(
                "Распределение трансферной стоимости",
                "Интервал стоимости, млн",
                "Игроков в интервале",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false,
            ).also { chart ->
                chart.backgroundPaint = Color.WHITE
                chart.title.font = Font("SansSerif", Font.BOLD, 22)
                chart.addSubtitle(
                    TextTitle("Каждый столбец - число игроков с ценой в соответствующем диапазоне 10 млн")
                        .apply { font = Font("SansSerif", Font.PLAIN, 14) },
                )
                chart.xyPlot.apply {
                    backgroundPaint = Color(248, 250, 252)
                    domainGridlinePaint = Color(220, 226, 234)
                    rangeGridlinePaint = Color(220, 226, 234)
                    (domainAxis as NumberAxis).tickUnit = NumberTickUnit(10.0)
                    (renderer as XYBarRenderer).setSeriesPaint(0, Color(48, 120, 180))
                }
            }
        }

    private fun byPositionChart(players: List<Player>) =
        DefaultBoxAndWhiskerCategoryDataset().also { dataset ->
            Position.entries.forEach { position ->
                val costs = players
                    .asSequence()
                    .filter { it.position == position }
                    .map { it.transferCost / MILLION }
                    .toList()
                if (costs.isNotEmpty()) dataset.add(costs, "Стоимость", position.russianName)
            }
        }.let { dataset: DefaultBoxAndWhiskerCategoryDataset ->
            ChartFactory.createBoxAndWhiskerChart(
                "Разброс трансферной стоимости по позициям",
                "Позиция",
                "Трансферная стоимость, млн",
                dataset,
                false,
            ).also { chart ->
                chart.backgroundPaint = Color.WHITE
                chart.title.font = Font("SansSerif", Font.BOLD, 22)
                chart.categoryPlot.apply {
                    backgroundPaint = Color(248, 250, 252)
                    rangeGridlinePaint = Color(220, 226, 234)
                    (rangeAxis as NumberAxis).autoRangeIncludesZero = false
                    (renderer as BoxAndWhiskerRenderer).apply {
                        fillBox = true
                        setMeanVisible(false)
                        setSeriesPaint(0, Color(44, 160, 116))
                        setSeriesOutlinePaint(0, Color(28, 95, 70))
                    }
                }
            }
        }
}
