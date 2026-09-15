package visualization


import model.Player
import model.Team
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator
import org.jfree.chart.plot.DatasetRenderingOrder
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.renderer.category.LineAndShapeRenderer
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.general.DefaultPieDataset
import java.awt.BasicStroke
import java.awt.Color
import java.io.File
import java.text.DecimalFormat

object CountryCharts {

    private const val CHART_WIDTH = 1400
    private const val CHART_HEIGHT = 850

    /**
     * Базовая визуализация:
     * доля игроков из каждой страны.
     */
    fun createCountryShareChart(
        players: List<Player>,
        outputFile: File
    ): File {
        require(players.isNotEmpty()) {
            "Нельзя построить диаграмму для пустого списка игроков"
        }

        val countryCounts = players
            .groupingBy { it.country }
            .eachCount()
            .entries
            .sortedByDescending { it.value }

        val dataset = DefaultPieDataset<String>()

        countryCounts.forEach { (country, count) ->
            dataset.setValue(country, count)
        }

        val chart = ChartFactory.createPieChart(
            "Доля игроков по странам",
            dataset,
            true,
            true,
            false
        )

        chart.backgroundPaint = Color.WHITE
        chart.title.font = chart.title.font.deriveFont(24f)
        chart.legend?.itemFont =
            chart.legend?.itemFont?.deriveFont(14f)

        prepareParentDirectory(outputFile)

        ChartUtils.saveChartAsPNG(
            outputFile,
            chart,
            CHART_WIDTH,
            CHART_HEIGHT
        )

        return outputFile
    }

    /**
     * Продвинутая визуализация:
     * топ-10 команд по стоимости и накопленная доля
     * от стоимости всех команд.
     */
    fun createTeamCostParetoChart(
        players: List<Player>,
        outputFile: File
    ): File {
        require(players.isNotEmpty()) {
            "Нельзя построить диаграмму для пустого списка игроков"
        }

        val allTeams = Team.aggregate(players)
            .sortedByDescending { it.totalTransferCost }

        val topTeams = allTeams.take(10)

        val totalCost = allTeams.sumOf {
            it.totalTransferCost
        }

        /*
         * runningFold возвращает начальный 0 и накопленные суммы.
         * drop(1) убирает начальное значение.
         */
        val cumulativeCosts = topTeams
            .map { it.totalTransferCost }
            .runningFold(0L) { accumulated, current ->
                accumulated + current
            }
            .drop(1)

        val cumulativeShares = cumulativeCosts.map { cost ->
            if (totalCost == 0L) {
                0.0
            } else {
                cost.toDouble() / totalCost * 100.0
            }
        }

        val costDataset = DefaultCategoryDataset()
        val shareDataset = DefaultCategoryDataset()

        topTeams.forEachIndexed { index, team ->
            val label = team.name

            costDataset.addValue(
                team.totalTransferCost / 1_000_000.0,
                "Стоимость состава",
                label
            )

            shareDataset.addValue(
                cumulativeShares[index],
                "Накопленная доля",
                label
            )
        }

        val chart = ChartFactory.createBarChart(
            "Топ-10 команд по стоимости и накопленная доля",
            "Команда",
            "Стоимость состава, млн",
            costDataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        )

        configureParetoChart(
            chart = chart,
            shareDataset = shareDataset
        )

        prepareParentDirectory(outputFile)

        ChartUtils.saveChartAsPNG(
            outputFile,
            chart,
            CHART_WIDTH,
            CHART_HEIGHT
        )

        return outputFile
    }

    private fun configureParetoChart(
        chart: JFreeChart,
        shareDataset: DefaultCategoryDataset
    ) {
        chart.backgroundPaint = Color.WHITE
        chart.title.font = chart.title.font.deriveFont(23f)

        val plot = chart.categoryPlot
        plot.backgroundPaint = Color(245, 245, 245)
        plot.domainGridlinePaint = Color.LIGHT_GRAY
        plot.rangeGridlinePaint = Color.LIGHT_GRAY

        plot.domainAxis.tickLabelFont =
            plot.domainAxis.tickLabelFont.deriveFont(11f)

        val barRenderer = plot.renderer
        barRenderer.defaultItemLabelsVisible = true
        barRenderer.defaultItemLabelGenerator =
            StandardCategoryItemLabelGenerator(
                "{2}",
                DecimalFormat("0.0")
            )

        val percentageAxis = NumberAxis(
            "Накопленная доля общей стоимости, %"
        ).apply {
            setRange(0.0, 100.0)
            numberFormatOverride = DecimalFormat("0.0'%'")
        }

        plot.setRangeAxis(1, percentageAxis)
        plot.setDataset(1, shareDataset)
        plot.mapDatasetToRangeAxis(1, 1)

        val lineRenderer = LineAndShapeRenderer(
            true,
            true
        ).apply {
            setSeriesPaint(0, Color(190, 35, 35))
            setSeriesStroke(
                0,
                BasicStroke(3.0f)
            )
            defaultItemLabelsVisible = true
            defaultItemLabelGenerator =
                StandardCategoryItemLabelGenerator(
                    "{2}%",
                    DecimalFormat("0.0")
                )
        }

        plot.setRenderer(1, lineRenderer)

        /*
         * Сначала отображаются столбцы, затем поверх них линия.
         */
        plot.datasetRenderingOrder =
            DatasetRenderingOrder.FORWARD
    }

    private fun prepareParentDirectory(
        outputFile: File
    ) {
        outputFile.parentFile?.mkdirs()
    }
}