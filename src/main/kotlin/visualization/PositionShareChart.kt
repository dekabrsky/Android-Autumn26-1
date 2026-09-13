package visualization

import org.jfree.chart.ChartFactory
import org.jfree.chart.JFreeChart
import org.jfree.chart.ChartUtils
import org.jfree.chart.plot.PiePlot
import org.jfree.data.general.DefaultPieDataset
import java.io.File

class PositionShareChart(private val distribution: PositionDistribution) {

    fun buildChart(): JFreeChart {
        val dataset = DefaultPieDataset<String>()

        distribution.counts.forEach { (positionName, count) ->
            dataset.setValue(positionName, count)
        }

        val chart = ChartFactory.createPieChart(
            "Доля игроков по позициям",
            dataset,
            true,   // legend
            true,   // tooltips
            false   // urls
        )

        val plot = chart.plot as PiePlot<String>
        plot.labelGenerator = org.jfree.chart.labels.StandardPieSectionLabelGenerator(
            "{0}: {1} ({2})"
        )

        return chart
    }

    fun render(outputPath: String = "position_share.png", width: Int = 800, height: Int = 600) {
        val chart = buildChart()
        ChartUtils.saveChartAsPNG(File(outputPath), chart, width, height)
    }
}