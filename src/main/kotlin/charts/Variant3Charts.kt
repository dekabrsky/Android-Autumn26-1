package charts

import model.Player
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.statistics.BoxAndWhiskerCalculator
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset
import org.jfree.data.statistics.HistogramDataset
import java.io.File

object Variant3Charts {

    fun createTransferCostDistribution(
        players: List<Player>,
        output: File = File("transfer_cost_distribution.png")
    ) {
        val dataset = HistogramDataset()
        val values = players.map { it.transferCost.toDouble() }.toDoubleArray()

        val min = values.minOrNull() ?: return
        val max = values.maxOrNull() ?: return

        dataset.addSeries(
            "Трансферная стоимость",
            values,
            10,
            min,
            if (min == max) max + 1.0 else max
        )

        val chart = ChartFactory.createHistogram(
            "Распределение трансферных стоимостей",
            "Трансферная стоимость",
            "Количество игроков",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false
        )

        ChartUtils.saveChartAsPNG(output, chart, 1100, 700)
    }

    fun createTransferCostSpreadByPosition(
        players: List<Player>,
        output: File = File("transfer_cost_by_position.png")
    ) {
        val dataset = DefaultBoxAndWhiskerCategoryDataset()

        players
            .groupBy { it.position }
            .toSortedMap(compareBy { it.russianName })
            .forEach { (position, group) ->
                dataset.add(
                    BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(
                        group.map { it.transferCost.toDouble() }
                    ),
                    "Стоимость",
                    position.russianName
                )
            }

        val chart = ChartFactory.createBoxAndWhiskerChart(
            "Разброс трансферных стоимостей по позициям",
            "Позиция",
            "Трансферная стоимость",
            dataset,
            false
        )

        ChartUtils.saveChartAsPNG(output, chart, 1100, 700)
    }
}
