package visualization

import model.Player
import model.Position
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.labels.ItemLabelAnchor
import org.jfree.chart.labels.ItemLabelPosition
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.renderer.category.BarRenderer
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.chart.ui.TextAnchor
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import java.awt.BasicStroke
import java.awt.Color
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.swing.JFrame
import javax.swing.SwingUtilities

object Variant2Charts {

    fun show(players: List<Player>) {
        SwingUtilities.invokeLater {
            showTop10Teams(players)
            showForwardPriceAndGoals(players)
        }
    }


    private fun showTop10Teams(players: List<Player>) {

        val top10Teams = players
            .groupBy { it.team }
            .map { (team, teamPlayers) ->
                team to teamPlayers.sumOf { it.transferCost.toLong() }
            }
            .sortedByDescending { it.second }
            .take(10)

        val dataset = DefaultCategoryDataset()

        top10Teams.forEach { (team, totalCost) ->
            dataset.addValue(
                totalCost.toDouble() / 1_000_000,
                "Стоимость",
                "${team.name} (${team.city})"
            )
        }

        val chart = ChartFactory.createBarChart(
            "Топ-10 команд по суммарной трансферной стоимости",
            "Команда",
            "Стоимость, млн",
            dataset,
            PlotOrientation.HORIZONTAL,
            false,
            true,
            false
        )

        val plot = chart.categoryPlot
        val renderer = plot.renderer as BarRenderer

        // Формат: 1386,4 млн
        val decimalFormat = DecimalFormat(
            "0.0",
            DecimalFormatSymbols(Locale("ru", "RU"))
        )

        renderer.setDefaultItemLabelGenerator(
            StandardCategoryItemLabelGenerator(
                "{2} млн",
                decimalFormat
            )
        )

        renderer.setDefaultItemLabelsVisible(true)


        renderer.setDefaultPositiveItemLabelPosition(
            ItemLabelPosition(
                ItemLabelAnchor.OUTSIDE3,
                TextAnchor.CENTER_LEFT
            )
        )


        plot.rangeAxis.upperMargin = 0.15

        val frame = JFrame("Задание 4 — базовый график")
        frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        frame.contentPane = ChartPanel(chart)
        frame.setSize(1050, 650)
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
    }


    private fun showForwardPriceAndGoals(players: List<Player>) {

        val forwards = players
            .filter { it.position == Position.FORWARD }
            .sortedBy { it.transferCost }

        val noCards = XYSeries("0 желтых карточек")
        val fewCards = XYSeries("1-2 желтые карточки")
        val mediumCards = XYSeries("3-4 желтые карточки")
        val manyCards = XYSeries("5+ желтых карточек")

        forwards.forEach { player ->

            val transferCostMillions =
                player.transferCost.toDouble() / 1_000_000

            when (player.yellowCards) {

                0 -> noCards.add(
                    transferCostMillions,
                    player.goals
                )

                in 1..2 -> fewCards.add(
                    transferCostMillions,
                    player.goals
                )

                in 3..4 -> mediumCards.add(
                    transferCostMillions,
                    player.goals
                )

                else -> manyCards.add(
                    transferCostMillions,
                    player.goals
                )
            }
        }

        val scatterDataset = XYSeriesCollection().apply {
            addSeries(noCards)
            addSeries(fewCards)
            addSeries(mediumCards)
            addSeries(manyCards)
        }

        val chart = ChartFactory.createScatterPlot(
            "Стоимость и результативность нападающих",
            "Трансферная стоимость, млн",
            "Количество голов",
            scatterDataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        )

        val plot = chart.xyPlot

        // Точки игроков
        val scatterRenderer =
            XYLineAndShapeRenderer(false, true)


        scatterRenderer.setSeriesPaint(
            0,
            Color(40, 160, 70)
        )


        scatterRenderer.setSeriesPaint(
            1,
            Color(220, 190, 0)
        )


        scatterRenderer.setSeriesPaint(
            2,
            Color(240, 130, 20)
        )


        scatterRenderer.setSeriesPaint(
            3,
            Color(210, 40, 40)
        )

        plot.renderer = scatterRenderer


        val movingAverageSeries =
            XYSeries("Скользящее среднее")

        forwards
            .windowed(
                size = 5,
                step = 1,
                partialWindows = false
            )
            .forEach { window ->

                val averageCost =
                    window
                        .map { it.transferCost.toDouble() }
                        .average() / 1_000_000

                val averageGoals =
                    window
                        .map { it.goals.toDouble() }
                        .average()

                movingAverageSeries.add(
                    averageCost,
                    averageGoals
                )
            }

        val movingAverageDataset =
            XYSeriesCollection().apply {
                addSeries(movingAverageSeries)
            }

        plot.setDataset(
            1,
            movingAverageDataset
        )

        val lineRenderer =
            XYLineAndShapeRenderer(true, false)

        lineRenderer.setSeriesPaint(
            0,
            Color.BLACK
        )

        lineRenderer.setSeriesStroke(
            0,
            BasicStroke(3.0f)
        )

        plot.setRenderer(
            1,
            lineRenderer
        )

        val frame =
            JFrame("Задание 4 — продвинутый график")

        frame.defaultCloseOperation =
            JFrame.DISPOSE_ON_CLOSE

        frame.contentPane =
            ChartPanel(chart)

        frame.setSize(
            1000,
            650
        )

        frame.setLocationRelativeTo(null)
        frame.isVisible = true
    }
}