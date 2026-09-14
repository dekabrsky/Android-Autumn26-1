package visualization

import model.Player
import model.Position
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset
import org.jfree.data.statistics.HistogramDataset
import org.jfree.data.statistics.HistogramType
import java.io.File

object Variant3Charts {

    fun save(
        players: List<Player>,
        outputDirectory: File = File("charts")
    ): List<File> {
        require(players.isNotEmpty()) {
            "Для построения графиков список игроков не должен быть пустым"
        }

        outputDirectory.mkdirs()

        return listOf(
            saveTransferValueHistogram(players, outputDirectory),
            saveTransferValueBoxPlot(players, outputDirectory)
        )
    }

    private fun saveTransferValueHistogram(
        players: List<Player>,
        outputDirectory: File
    ): File {
        val valuesInMillions = players
            .map { it.transferValue / 1_000_000.0 }
            .toDoubleArray()

        val dataset = HistogramDataset().apply {
            type = HistogramType.FREQUENCY
            addSeries(
                "Игроки",
                valuesInMillions,
                15
            )
        }

        val chart = ChartFactory.createHistogram(
            "Распределение трансферных стоимостей",
            "Трансферная стоимость, млн",
            "Количество игроков",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        )

        val file = File(
            outputDirectory,
            "transfer-cost-histogram.png"
        )

        ChartUtils.saveChartAsPNG(file, chart, 1200, 700)
        return file
    }

    private fun saveTransferValueBoxPlot(
        players: List<Player>,
        outputDirectory: File
    ): File {
        val dataset = DefaultBoxAndWhiskerCategoryDataset()

        Position.entries.forEach { position ->
            val values = players
                .filter { it.position == position }
                .map { it.transferValue / 1_000_000.0 }

            if (values.isNotEmpty()) {
                dataset.add(
                    values,
                    "Стоимость",
                    position.russianName
                )
            }
        }

        val chart = ChartFactory.createBoxAndWhiskerChart(
            "Разброс трансферных стоимостей по позициям",
            "Позиция",
            "Трансферная стоимость, млн",
            dataset,
            true
        )

        val file = File(
            outputDirectory,
            "transfer-cost-by-position.png"
        )

        ChartUtils.saveChartAsPNG(file, chart, 1200, 700)
        return file
    }
}