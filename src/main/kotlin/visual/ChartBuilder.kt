package visual


import model.Player
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer
import org.jfree.data.statistics.HistogramDataset
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset
import javax.swing.JFrame
import javax.swing.SwingUtilities

object ChartBuilder {

    fun showTransferCostDistribution(players: List<Player>) {
        val costs = players
            .map { it.transferCost.toDouble() / 1_000_000.0 }
            .toDoubleArray()

        val dataset = HistogramDataset()

        dataset.addSeries(
            "Игроки",
            costs,
            10
        )

        val chart = ChartFactory.createHistogram(
            "Распределение трансферных стоимостей",
            "Трансферная стоимость, млн",
            "Количество игроков",
            dataset
        )

        showChart(
            chart,
            "Распределение трансферных стоимостей"
        )
    }


    fun showTransferCostByPosition(players: List<Player>) {

        val dataset = DefaultBoxAndWhiskerCategoryDataset()

        val playersByPosition = players.groupBy { it.position }

        for ((position, positionPlayers) in playersByPosition) {

            val costs = positionPlayers.map {
                it.transferCost.toDouble() / 1_000_000.0
            }

            dataset.add(
                costs,
                "Трансферная стоимость",
                position.russionPosition
            )
        }

        val chart = ChartFactory.createBoxAndWhiskerChart(
            "Разброс трансферных стоимостей по позициям",
            "Позиция",
            "Трансферная стоимость, млн",
            dataset,
            false
        )

        val renderer = chart.categoryPlot.renderer as BoxAndWhiskerRenderer
        renderer.isMeanVisible = false

        showChart(
            chart,
            "Разброс трансферных стоимостей по позициям"
        )
    }


    private fun showChart(
        chart: JFreeChart,
        title: String
    ) {
        SwingUtilities.invokeLater {

            val frame = JFrame(title)

            frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
            frame.contentPane = ChartPanel(chart)
            frame.setSize(900, 600)
            frame.setLocationRelativeTo(null)
            frame.isVisible = true
        }
    }
}

