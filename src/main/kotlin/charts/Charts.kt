package charts

import model.Player
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset
import org.jfree.data.statistics.HistogramDataset
import org.jfree.chart.axis.NumberAxis
import java.awt.Dimension
import javax.swing.JFrame

object Charts {

    fun showTransferCostDistribution(players: List<Player>) {
        val costs = players.map { it.transferCost.toDouble() }

        val dataset = HistogramDataset()
        dataset.addSeries(
            "Игроки",
            costs.toDoubleArray(),
            10
        )

        val chart = ChartFactory.createHistogram(
            "Распределение трансферных стоимостей",
            "Трансферная стоимость",
            "Количество игроков",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        )

        val plot = chart.plot as org.jfree.chart.plot.XYPlot
        val axis = plot.domainAxis as NumberAxis

        axis.numberFormatOverride =
            java.text.DecimalFormat("0")

        showChart("Распределение трансферных стоимостей", chart)
    }

    fun showTransferCostSpreadByPosition(players: List<Player>) {
        val dataset = DefaultBoxAndWhiskerCategoryDataset()

        val positions = players
            .groupBy { it.position }
            .toSortedMap(compareBy { it.name })

        positions.forEach { (position, positionPlayers) ->
            val costs = positionPlayers.map { it.transferCost.toDouble() }

            dataset.add(
                costs,
                "Игроки",
                position.rusName
            )
        }

        val chart = ChartFactory.createBoxAndWhiskerChart(
            "Разброс трансферных стоимостей по позициям",
            "Позиция",
            "Трансферная стоимость",
            dataset,
            true
        )

        showChart("Разброс трансферных стоимостей по позициям", chart)
    }

    private fun showChart(title: String, chart: JFreeChart) {
        val frame = JFrame(title)
        frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        frame.contentPane.add(ChartPanel(chart))
        frame.preferredSize = Dimension(1000, 700)
        frame.pack()
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
    }
}