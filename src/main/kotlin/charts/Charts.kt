package charts

import model.Player
import model.Position
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.CategoryLabelPositions
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.category.BarRenderer
import org.jfree.chart.renderer.category.StandardBarPainter
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.geom.Ellipse2D
import java.io.File
import java.text.DecimalFormat

/**
 * Задание 4, вариант 2 «Цена и результативность» (JFreeChart).
 *
 * - Базовая: топ-10 команд по суммарной трансферной стоимости.
 * - Продвинутая: связь трансферной стоимости и числа голов у нападающих
 *   со скользящим средним (тренд) и жёлтыми карточками, показанными цветом.
 */
object Charts {

    private const val WIDTH = 1200
    private const val HEIGHT = 700

    private val titleFont = Font("SansSerif", Font.BOLD, 20)
    private val labelFont = Font("SansSerif", Font.PLAIN, 13)

    /** Строит оба графика и сохраняет их как PNG в [outputDir]. Возвращает созданные файлы. */
    fun renderAll(players: List<Player>, outputDir: File): List<File> {
        outputDir.mkdirs()
        val basic = File(outputDir, "top10_teams_by_cost.png")
        val advanced = File(outputDir, "forwards_cost_vs_goals.png")
        ChartUtils.saveChartAsPNG(basic, topTeamsByCostChart(players), WIDTH, HEIGHT)
        ChartUtils.saveChartAsPNG(advanced, forwardsCostVsGoalsChart(players), WIDTH, HEIGHT)
        return listOf(basic, advanced)
    }

    /** Топ-10 команд по суммарной трансферной стоимости (в млн). */
    fun topTeamsByCost(players: List<Player>, limit: Int = 10): List<Pair<String, Long>> =
        players
            .groupBy { it.team }
            .map { (team, teamPlayers) -> team.name to teamPlayers.sumOf { it.transferCost } }
            .sortedByDescending { it.second }
            .take(limit)

    fun topTeamsByCostChart(players: List<Player>): JFreeChart {
        val dataset = DefaultCategoryDataset()
        topTeamsByCost(players).forEach { (team, cost) ->
            dataset.addValue(cost / 1_000_000.0, "Суммарная стоимость", team)
        }

        val chart = ChartFactory.createBarChart(
            "Топ-10 команд по суммарной трансферной стоимости",
            "Команда",
            "Стоимость, млн",
            dataset,
            PlotOrientation.VERTICAL,
            false, true, false,
        )
        chart.title.font = titleFont
        chart.backgroundPaint = Color.WHITE

        val plot = chart.categoryPlot
        plot.backgroundPaint = Color.WHITE
        plot.rangeGridlinePaint = Color(220, 220, 220)
        plot.domainAxis.categoryLabelPositions = CategoryLabelPositions.UP_45
        plot.domainAxis.tickLabelFont = labelFont
        plot.rangeAxis.tickLabelFont = labelFont
        (plot.rangeAxis as NumberAxis).numberFormatOverride = DecimalFormat("#,##0")

        val renderer = plot.renderer as BarRenderer
        renderer.barPainter = StandardBarPainter()
        renderer.setSeriesPaint(0, Color(46, 110, 196))
        renderer.isDrawBarOutline = false
        renderer.setDefaultItemLabelsVisible(true)
        renderer.setDefaultItemLabelGenerator(
            org.jfree.chart.labels.StandardCategoryItemLabelGenerator("{2}", DecimalFormat("#,##0.0")),
        )
        return chart
    }

    /** Нападающие, отсортированные по стоимости, — основа для точек и тренда. */
    fun forwardsByCost(players: List<Player>): List<Player> =
        players.filter { it.position == Position.FORWARD }.sortedBy { it.transferCost }

    /**
     * Скользящее среднее числа голов по окну из [window] нападающих (по возрастанию стоимости).
     * Возвращает пары (средняя стоимость окна, среднее число голов окна).
     */
    fun movingAverage(forwards: List<Player>, window: Int = 7): List<Pair<Double, Double>> =
        forwards
            .windowed(size = window, step = 1)
            .map { win -> win.map { it.transferCost }.average() to win.map { it.goals }.average() }

    /** Группа по жёлтым карточкам: определяет цвет точки. */
    private enum class YellowBucket(val label: String, val color: Color, val range: IntRange) {
        LOW("0–3 жёлтых карточки", Color(52, 168, 83), 0..3),
        MEDIUM("4–6 жёлтых карточек", Color(251, 188, 4), 4..6),
        HIGH("7+ жёлтых карточек", Color(217, 48, 37), 7..Int.MAX_VALUE);

        companion object {
            fun of(yellowCards: Int): YellowBucket = entries.first { yellowCards in it.range }
        }
    }

    fun forwardsCostVsGoalsChart(players: List<Player>): JFreeChart {
        val forwards = forwardsByCost(players)
        val toMillions = { cost: Number -> cost.toDouble() / 1_000_000.0 }

        val dataset = XYSeriesCollection()
        val bucketSeries = YellowBucket.entries.associateWith { XYSeries(it.label) }
        forwards.forEach { p ->
            bucketSeries.getValue(YellowBucket.of(p.yellowCards)).add(toMillions(p.transferCost), p.goals.toDouble())
        }
        bucketSeries.values.forEach { dataset.addSeries(it) }

        val trend = XYSeries("Скользящее среднее голов (окно 7)")
        movingAverage(forwards).forEach { (cost, goals) -> trend.add(toMillions(cost), goals) }
        dataset.addSeries(trend)

        val chart = ChartFactory.createScatterPlot(
            "Нападающие: трансферная стоимость и голы (цвет — жёлтые карточки)",
            "Трансферная стоимость, млн",
            "Голы",
            dataset,
            PlotOrientation.VERTICAL,
            true, true, false,
        )
        chart.title.font = titleFont
        chart.backgroundPaint = Color.WHITE

        val plot = chart.plot as XYPlot
        plot.backgroundPaint = Color.WHITE
        plot.domainGridlinePaint = Color(220, 220, 220)
        plot.rangeGridlinePaint = Color(220, 220, 220)
        plot.domainAxis.tickLabelFont = labelFont
        plot.rangeAxis.tickLabelFont = labelFont

        val renderer = XYLineAndShapeRenderer()
        val dot = Ellipse2D.Double(-5.0, -5.0, 10.0, 10.0)
        YellowBucket.entries.forEachIndexed { i, bucket ->
            renderer.setSeriesLinesVisible(i, false)
            renderer.setSeriesShapesVisible(i, true)
            renderer.setSeriesShape(i, dot)
            renderer.setSeriesPaint(i, bucket.color)
        }
        val trendIndex = YellowBucket.entries.size
        renderer.setSeriesLinesVisible(trendIndex, true)
        renderer.setSeriesShapesVisible(trendIndex, false)
        renderer.setSeriesPaint(trendIndex, Color(30, 30, 30))
        renderer.setSeriesStroke(trendIndex, BasicStroke(3f))
        plot.renderer = renderer
        return chart
    }
}
