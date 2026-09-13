import model.Player
import model.Position
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset
import org.jfree.data.statistics.HistogramDataset
import java.io.File

object Charts {
    fun saveTransferCostHistogram(players: List<Player>, file: File) {
        val dataset = HistogramDataset()
        val values = players.map { it.transferCost.toDouble() / 1_000_000 }.toDoubleArray()
        dataset.addSeries("Игроки", values, 10)

        val chart = ChartFactory.createHistogram(
            "Распределение трансферной стоимости",
            "Стоимость, млн",
            "Количество игроков",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false
        )

        ChartUtils.saveChartAsPNG(file, chart, 1000, 600)
    }

    fun saveTransferCostByPosition(players: List<Player>, file: File) {
        val dataset = DefaultBoxAndWhiskerCategoryDataset()

        Position.entries.forEach { position ->
            val values = players
                .filter { it.position == position }
                .map { it.transferCost.toDouble() / 1_000_000 }

            if (values.isNotEmpty()) {
                dataset.add(values, "Стоимость", position.russianName)
            }
        }

        val chart = ChartFactory.createBoxAndWhiskerChart(
            "Трансферная стоимость по позициям",
            "Позиция",
            "Стоимость, млн",
            dataset,
            false
        )

        ChartUtils.saveChartAsPNG(file, chart, 1000, 600)
    }
}
