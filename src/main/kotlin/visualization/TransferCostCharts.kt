package visualization

import model.Player
import model.Position
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset
import org.jfree.data.statistics.HistogramDataset
import java.nio.file.Files
import java.nio.file.Path

object TransferCostCharts {
    fun saveAll(players: List<Player>, outputDirectory: Path): List<Path> {
        Files.createDirectories(outputDirectory)
        return listOf(
            saveDistribution(players, outputDirectory.resolve("transfer-cost-distribution.png")),
            savePositionComparison(players, outputDirectory.resolve("transfer-cost-by-position.png")),
        )
    }

    fun saveDistribution(players: List<Player>, output: Path): Path {
        val dataset = HistogramDataset().apply {
            val values = players.map { it.transferCost / 1_000_000.0 }.toDoubleArray()
            if (values.isNotEmpty()) addSeries("Игроки", values, 12)
        }
        val chart = ChartFactory.createHistogram(
            "Распределение трансферной стоимости",
            "Стоимость, млн",
            "Количество игроков",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false,
        )
        ChartUtils.saveChartAsPNG(output.toFile(), chart, 1200, 700)
        return output
    }

    fun savePositionComparison(players: List<Player>, output: Path): Path {
        val dataset = DefaultBoxAndWhiskerCategoryDataset()
        Position.entries.forEach { position ->
            val values = players
                .filter { it.position == position }
                .map { it.transferCost / 1_000_000.0 }
            if (values.isNotEmpty()) dataset.add(values, "Стоимость", position.russianName)
        }
        val chart = ChartFactory.createBoxAndWhiskerChart(
            "Стоимость игроков по позициям",
            "Позиция",
            "Стоимость, млн",
            dataset,
            false,
        )
        (chart.categoryPlot.renderer as BoxAndWhiskerRenderer).isMeanVisible = false
        ChartUtils.saveChartAsPNG(output.toFile(), chart, 1200, 700)
        return output
    }
}
