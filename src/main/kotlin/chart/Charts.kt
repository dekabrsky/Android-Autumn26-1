package chart

import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.CategoryLabelPositions
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator
import org.jfree.chart.labels.StandardPieSectionLabelGenerator
import org.jfree.chart.plot.DatasetRenderingOrder
import org.jfree.chart.plot.PiePlot
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.renderer.category.BarRenderer
import org.jfree.chart.renderer.category.LineAndShapeRenderer
import org.jfree.chart.renderer.category.StandardBarPainter
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.general.DefaultPieDataset
import java.awt.BasicStroke
import java.awt.Color
import java.io.File
import java.text.DecimalFormat
import java.text.NumberFormat

object Charts {

    fun countrySharePie(shares: List<CountryShare>): JFreeChart {
        val dataset = DefaultPieDataset<String>()
        shares.forEach { dataset.setValue(it.country, it.playersCount) }

        val chart = ChartFactory.createPieChart("Доля игроков по странам", dataset, true, true, false)
        (chart.plot as PiePlot<*>).apply {
            labelGenerator = StandardPieSectionLabelGenerator(
                "{0}: {1} ({2})", NumberFormat.getIntegerInstance(), DecimalFormat("0.0%"),
            )
            backgroundPaint = Color.WHITE
            setOutlineVisible(false)
        }
        return chart
    }

    // Диаграмма Парето: столбцы — стоимость состава, линия — накопленная доля от общей стоимости.
    fun topTeamsPareto(teams: List<TeamCostShare>): JFreeChart {
        val costDataset = DefaultCategoryDataset()
        val shareDataset = DefaultCategoryDataset()
        teams.forEach {
            val label = "${it.team.name} (${it.team.city})"
            costDataset.addValue(it.totalCost / 1_000_000.0, "Суммарная стоимость состава, млн", label)
            shareDataset.addValue(it.cumulativeShare * 100, "Накопленная доля от общей стоимости, %", label)
        }

        val chart = ChartFactory.createBarChart(
            "Топ-${teams.size} команд по суммарной трансферной стоимости",
            "Команда",
            "Стоимость состава, млн",
            costDataset,
            PlotOrientation.VERTICAL,
            true, true, false,
        )

        chart.categoryPlot.apply {
            backgroundPaint = Color.WHITE
            rangeGridlinePaint = Color.LIGHT_GRAY
            domainAxis.categoryLabelPositions = CategoryLabelPositions.UP_45
            (renderer as BarRenderer).apply {
                barPainter = StandardBarPainter()
                setShadowVisible(false)
                setSeriesPaint(0, Color(0x4C78A8))
                defaultItemLabelGenerator = StandardCategoryItemLabelGenerator("{2}", DecimalFormat("0"))
                defaultItemLabelsVisible = true
            }

            setDataset(1, shareDataset)
            setRangeAxis(1, NumberAxis("Накопленная доля, %").apply { setRange(0.0, 105.0) })
            mapDatasetToRangeAxis(1, 1)
            setRenderer(1, LineAndShapeRenderer().apply {
                setSeriesPaint(0, Color(0xE45756))
                setSeriesStroke(0, BasicStroke(3f))
                defaultItemLabelGenerator = StandardCategoryItemLabelGenerator("{2}%", DecimalFormat("0.0"))
                defaultItemLabelsVisible = true
            })
            // Линия поверх столбцов.
            datasetRenderingOrder = DatasetRenderingOrder.FORWARD
        }
        return chart
    }

    fun save(chart: JFreeChart, file: File, width: Int = 1200, height: Int = 800) {
        file.parentFile?.mkdirs()
        ChartUtils.saveChartAsPNG(file, chart, width, height)
    }
}
