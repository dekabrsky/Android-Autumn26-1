package visualization

import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.labels.StandardPieSectionLabelGenerator
import org.jfree.chart.plot.DatasetRenderingOrder
import org.jfree.chart.plot.PiePlot
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.renderer.category.BarRenderer
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.general.DefaultPieDataset
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.io.File
import java.text.FieldPosition
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParsePosition

fun saveCountryShareChart(countryShares: List<CountryShare>, file: File) {
    val dataset = DefaultPieDataset<String>()
    countryShares.forEach { countryShare ->
        dataset.setValue(countryShare.country, countryShare.playersCount)
    }

    val chart = ChartFactory.createPieChart(
        "Доля игроков по странам",
        dataset,
        true,
        true,
        false,
    )
    val plot = chart.plot as PiePlot<*>
    plot.labelGenerator = StandardPieSectionLabelGenerator(
        "{0}: {1} ({2})",
        DecimalFormat("0"),
        DecimalFormat("0.0%"),
    )
    plot.labelFont = LABEL_FONT
    applyCommonFonts(chart)

    saveChart(chart, file)
}

fun saveTeamCostCumulativeShareChart(teamCostShares: List<TeamCostShare>, file: File) {
    val costDataset = DefaultCategoryDataset()
    teamCostShares.forEach { item ->
        costDataset.addValue(item.totalCost, "Стоимость состава", item.team.name)
    }

    val chart = ChartFactory.createBarChart(
        "Топ-10 команд по стоимости и накопленная доля",
        "Команда",
        "Суммарная стоимость",
        costDataset,
        PlotOrientation.VERTICAL,
        true,
        true,
        false,
    )

    val plot = chart.categoryPlot
    (plot.rangeAxis as NumberAxis).numberFormatOverride = MillionsNumberFormat()
    plot.domainAxis.labelFont = AXIS_LABEL_FONT
    plot.domainAxis.tickLabelFont = AXIS_TICK_FONT
    plot.rangeAxis.labelFont = AXIS_LABEL_FONT
    plot.rangeAxis.tickLabelFont = AXIS_TICK_FONT

    val barRenderer = plot.renderer as BarRenderer
    barRenderer.setSeriesPaint(0, Color(0xB8, 0x5C, 0x5C))
    barRenderer.barPainter = org.jfree.chart.renderer.category.StandardBarPainter()
    barRenderer.setShadowVisible(false)

    val shareDataset = DefaultCategoryDataset()
    teamCostShares.forEach { item ->
        shareDataset.addValue(item.cumulativeShare * 100, "Накопленная доля, %", item.team.name)
    }

    val shareAxis = NumberAxis("Накопленная доля, %")
    shareAxis.range = org.jfree.data.Range(0.0, 100.0)
    shareAxis.numberFormatOverride = DecimalFormat("0")
    shareAxis.labelFont = AXIS_LABEL_FONT
    shareAxis.tickLabelFont = AXIS_TICK_FONT
    plot.setRangeAxis(1, shareAxis)
    plot.setDataset(1, shareDataset)
    plot.mapDatasetToRangeAxis(1, 1)

    val lineRenderer = org.jfree.chart.renderer.category.LineAndShapeRenderer()
    lineRenderer.setSeriesPaint(0, Color(0x32, 0xA8, 0x52))
    lineRenderer.setSeriesStroke(0, BasicStroke(2.5f))
    plot.setRenderer(1, lineRenderer)
    plot.datasetRenderingOrder = DatasetRenderingOrder.FORWARD
    applyCommonFonts(chart)

    saveChart(chart, file, width = 1200, height = 700)
}

private fun applyCommonFonts(chart: JFreeChart) {
    chart.title.font = TITLE_FONT
    chart.legend?.itemFont = LEGEND_FONT
}

private fun saveChart(chart: JFreeChart, file: File, width: Int = 1000, height: Int = 700) {
    file.parentFile?.mkdirs()
    ChartUtils.saveChartAsPNG(file, chart, width, height)
}

private val TITLE_FONT = Font("Segoe UI", Font.BOLD, 22)
private val LEGEND_FONT = Font("Segoe UI", Font.PLAIN, 13)
private val LABEL_FONT = Font("Segoe UI", Font.PLAIN, 13)
private val AXIS_LABEL_FONT = Font("Segoe UI", Font.BOLD, 14)
private val AXIS_TICK_FONT = Font("Segoe UI", Font.PLAIN, 11)

private class MillionsNumberFormat : NumberFormat() {
    private val decimalFormat = DecimalFormat("0.#")

    override fun format(number: Double, toAppendTo: StringBuffer, pos: FieldPosition): StringBuffer =
        toAppendTo.append(decimalFormat.format(number / 1_000_000)).append(" млн")

    override fun format(number: Long, toAppendTo: StringBuffer, pos: FieldPosition): StringBuffer =
        format(number.toDouble(), toAppendTo, pos)

    override fun parse(source: String, parsePosition: ParsePosition): Number? = null
}
