package visualization

import model.Player
import model.Position
import model.Team
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.renderer.category.BarRenderer
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import java.awt.BasicStroke
import java.awt.Color
import java.awt.geom.Ellipse2D
import java.nio.file.Files
import java.nio.file.Path
import java.text.DecimalFormat

object Variant2Charts {
    private const val CHART_WIDTH = 1_200
    private const val CHART_HEIGHT = 800
    private const val MOVING_AVERAGE_WINDOW = 7
    private const val MILLION = 1_000_000.0

    fun generate(
        players: List<Player>,
        outputDirectory: Path = Path.of("build", "charts"),
    ): List<Path> {
        require(players.isNotEmpty()) { "Невозможно построить графики без игроков" }
        Files.createDirectories(outputDirectory)

        val topTeamsPath = outputDirectory.resolve("variant2-top-teams.png")
        val forwardsPath = outputDirectory.resolve("variant2-forward-cost-and-goals.png")

        ChartUtils.saveChartAsPNG(topTeamsPath.toFile(), createTopTeamsChart(players), CHART_WIDTH, CHART_HEIGHT)
        ChartUtils.saveChartAsPNG(forwardsPath.toFile(), createForwardCostAndGoalsChart(players), CHART_WIDTH, CHART_HEIGHT)

        return listOf(topTeamsPath, forwardsPath)
    }

    private fun topTeams(players: List<Player>): List<Team> = players
        .groupBy(Player::team)
        .map { (team, teamPlayers) -> Team(team.name, team.city, teamPlayers) }
        .sortedByDescending { it.totalTransferCost }
        .take(10)

    private fun movingAverage(players: List<Player>): List<Pair<Double, Double>> = players
        .filter { it.position == Position.FORWARD }
        .sortedBy(Player::transferCost)
        .windowed(MOVING_AVERAGE_WINDOW)
        .map { window ->
            window.map(Player::transferCost).average() / MILLION to
                window.map(Player::goals).average()
        }

    private fun createTopTeamsChart(players: List<Player>): JFreeChart {
        val dataset = DefaultCategoryDataset()
        topTeams(players).forEach { team ->
            dataset.addValue(team.totalTransferCost / MILLION, "Стоимость", team.name)
        }

        val chart = ChartFactory.createBarChart(
            "Топ-10 команд по суммарной трансферной стоимости",
            "Команда",
            "Стоимость состава, млн",
            dataset,
            PlotOrientation.HORIZONTAL,
            false,
            true,
            false,
        )
        val plot = chart.categoryPlot
        val renderer = plot.renderer as BarRenderer
        renderer.setSeriesPaint(0, Color(46, 125, 186))
        renderer.defaultItemLabelGenerator =
            StandardCategoryItemLabelGenerator("{2} млн", DecimalFormat("0.0"))
        renderer.defaultItemLabelsVisible = true
        plot.backgroundPaint = Color(248, 250, 252)
        plot.rangeGridlinePaint = Color(210, 215, 220)
        (plot.rangeAxis as NumberAxis).apply {
            numberFormatOverride = DecimalFormat("0")
            upperMargin = 0.12
        }
        return chart
    }

    private fun createForwardCostAndGoalsChart(players: List<Player>): JFreeChart {
        val forwards = players.filter { it.position == Position.FORWARD }
        require(forwards.isNotEmpty()) { "В выборке нет нападающих" }

        val dataset = XYSeriesCollection()
        val cardValues = forwards.map(Player::yellowCards).distinct().sorted()
        cardValues.forEach { cards ->
            val series = XYSeries("$cards жёлтых")
            forwards
                .filter { it.yellowCards == cards }
                .forEach { player ->
                    series.add(player.transferCost / MILLION, player.goals)
                }
            dataset.addSeries(series)
        }

        val trend = XYSeries("Скользящее среднее ($MOVING_AVERAGE_WINDOW игроков)")
        movingAverage(forwards).forEach { (cost, goals) -> trend.add(cost, goals) }
        dataset.addSeries(trend)

        val chart = ChartFactory.createScatterPlot(
            "Стоимость и результативность нападающих",
            "Трансферная стоимость, млн",
            "Количество голов",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false,
        )
        val plot = chart.xyPlot
        val renderer = XYLineAndShapeRenderer(false, true)
        val maxCards = cardValues.maxOrNull()?.coerceAtLeast(1) ?: 1
        cardValues.indices.forEach { seriesIndex ->
            val cards = cardValues[seriesIndex]
            renderer.setSeriesPaint(seriesIndex, cardColor(cards, maxCards))
            renderer.setSeriesShape(seriesIndex, Ellipse2D.Double(-4.5, -4.5, 9.0, 9.0))
            renderer.setSeriesLinesVisible(seriesIndex, false)
            renderer.setSeriesShapesVisible(seriesIndex, true)
        }

        val trendIndex = cardValues.size
        renderer.setSeriesPaint(trendIndex, Color(28, 35, 49))
        renderer.setSeriesStroke(trendIndex, BasicStroke(3.0f))
        renderer.setSeriesLinesVisible(trendIndex, true)
        renderer.setSeriesShapesVisible(trendIndex, false)
        plot.renderer = renderer
        plot.backgroundPaint = Color(248, 250, 252)
        plot.domainGridlinePaint = Color(220, 224, 228)
        plot.rangeGridlinePaint = Color(220, 224, 228)
        (plot.domainAxis as NumberAxis).numberFormatOverride = DecimalFormat("0")
        (plot.rangeAxis as NumberAxis).numberFormatOverride = DecimalFormat("0")
        return chart
    }

    private fun cardColor(cards: Int, maxCards: Int): Color {
        val ratio = cards.toFloat() / maxCards
        val hue = 0.58f * (1.0f - ratio)
        return Color.getHSBColor(hue, 0.78f, 0.82f)
    }

}
