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

object FootballCharts {

    fun showCharts(players: List<Player>) {
        SwingUtilities.invokeLater {
            showTeamCostChart(players)
            showForwardEfficiencyChart(players)
        }
    }

    private fun showTeamCostChart(players: List<Player>) {
        val topTeams = players
            .groupBy { it.team }
            .map { (team, teamPlayers) ->
                team to teamPlayers.sumOf { it.transferCost.toLong() }
            }
            .sortedByDescending { it.second }
            .take(10)

        val dataset = DefaultCategoryDataset()

        topTeams.forEach { (team, totalCost) ->
            dataset.addValue(
                totalCost.toDouble() / 1_000_000,
                "Общая стоимость",
                "${team.name} (${team.city})"
            )
        }

        val chart = ChartFactory.createBarChart(
            "10 команд с наибольшей суммарной стоимостью состава",
            "Команда",
            "Стоимость состава, млн",
            dataset,
            PlotOrientation.HORIZONTAL,
            false,
            true,
            false
        )

        chart.backgroundPaint = Color.WHITE

        val plot = chart.categoryPlot
        plot.backgroundPaint = Color.WHITE
        plot.domainGridlinePaint = Color(220, 220, 220)
        plot.rangeGridlinePaint = Color(220, 220, 220)

        val renderer = plot.renderer as BarRenderer

        renderer.setSeriesPaint(
            0,
            Color(52, 122, 183)
        )

        val format = DecimalFormat(
            "0.0",
            DecimalFormatSymbols(Locale("ru", "RU"))
        )

        renderer.setDefaultItemLabelGenerator(
            StandardCategoryItemLabelGenerator(
                "{2} млн",
                format
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

        val frame = JFrame("Задание 4 — стоимость команд")
        frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        frame.contentPane = ChartPanel(chart)
        frame.setSize(1050, 650)
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
    }

    private fun showForwardEfficiencyChart(players: List<Player>) {
        val forwards = players
            .filter { it.position == Position.FORWARD }
            .sortedBy { it.transferCost }

        val noCards = XYSeries("Без желтых карточек")
        val oneTwoCards = XYSeries("1-2 желтые")
        val threeFourCards = XYSeries("3-4 желтые")
        val fivePlusCards = XYSeries("5 и более желтых")

        forwards.forEach { player ->
            val cost = player.transferCost.toDouble() / 1_000_000

            when (player.yellowCards) {
                0 -> noCards.add(cost, player.goals)
                in 1..2 -> oneTwoCards.add(cost, player.goals)
                in 3..4 -> threeFourCards.add(cost, player.goals)
                else -> fivePlusCards.add(cost, player.goals)
            }
        }

        val scatterData = XYSeriesCollection().apply {
            addSeries(noCards)
            addSeries(oneTwoCards)
            addSeries(threeFourCards)
            addSeries(fivePlusCards)
        }

        val chart = ChartFactory.createScatterPlot(
            "Нападающие: стоимость, голы и дисциплина",
            "Стоимость игрока, млн",
            "Голы",
            scatterData,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        )

        chart.backgroundPaint = Color.WHITE

        val plot = chart.xyPlot
        plot.backgroundPaint = Color.WHITE
        plot.domainGridlinePaint = Color(225, 225, 225)
        plot.rangeGridlinePaint = Color(225, 225, 225)

        val pointRenderer = XYLineAndShapeRenderer(false, true)

        pointRenderer.setSeriesPaint(0, Color(46, 160, 67))
        pointRenderer.setSeriesPaint(1, Color(255, 193, 7))
        pointRenderer.setSeriesPaint(2, Color(255, 127, 14))
        pointRenderer.setSeriesPaint(3, Color(214, 39, 40))

        plot.renderer = pointRenderer

        val trendSeries = XYSeries("Скользящее среднее")

        forwards
            .windowed(
                size = 5,
                step = 1,
                partialWindows = false
            )
            .forEach { window ->
                val averageCost =
                    window.map { it.transferCost.toDouble() }
                        .average() / 1_000_000

                val averageGoals =
                    window.map { it.goals.toDouble() }
                        .average()

                trendSeries.add(
                    averageCost,
                    averageGoals
                )
            }

        val trendDataset = XYSeriesCollection().apply {
            addSeries(trendSeries)
        }

        plot.setDataset(
            1,
            trendDataset
        )

        val trendRenderer =
            XYLineAndShapeRenderer(true, false)

        trendRenderer.setSeriesPaint(
            0,
            Color(70, 70, 70)
        )

        trendRenderer.setSeriesStroke(
            0,
            BasicStroke(2.5f)
        )

        plot.setRenderer(
            1,
            trendRenderer
        )

        val frame = JFrame("Задание 4 — анализ результативности")
        frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        frame.contentPane = ChartPanel(chart)
        frame.setSize(1000, 650)
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
    }
}