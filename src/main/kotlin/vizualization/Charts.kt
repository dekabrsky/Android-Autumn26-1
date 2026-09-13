package vizualization

import model.Player
import model.Position
import model.Team
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartFrame
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import org.jfree.chart.labels.ItemLabelAnchor
import org.jfree.chart.labels.ItemLabelPosition
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator
import org.jfree.chart.labels.StandardPieSectionLabelGenerator
import org.jfree.chart.plot.PiePlot
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.renderer.category.BarRenderer
import org.jfree.chart.ui.TextAnchor
import org.jfree.data.category.CategoryDataset
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.general.DefaultPieDataset
import java.io.File
import java.text.DecimalFormat

object Charts {

    fun positionShareChart(players: List<Player>): JFreeChart {
        val dataset = DefaultPieDataset<String>()
        players.groupingBy { it.position }.eachCount()
            .entries
            .sortedByDescending { it.value }
            .forEach { (position, count) -> dataset.setValue(position.russianName, count) }

        val chart = ChartFactory.createPieChart(
            "Доля игроков по позициям",
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

        return chart
    }

    fun topTeamsPositionCompositionChart(teams: List<Team>, topN: Int = 5): JFreeChart {
        val topTeams = teams.sortedByDescending { it.totalTransferCost }.take(topN)
        val dataset = DefaultCategoryDataset()

        Position.entries.forEach { position ->
            topTeams.forEach { team ->
                val count = team.players.count { it.position == position }
                dataset.addValue(count.toDouble(), position.russianName, team.name)
            }
        }

        val chart = ChartFactory.createStackedBarChart(
            "Состав топ-5 команд по трансферной стоимости",
            "Команда",
            "Количество игроков",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false,
        )

        val renderer = chart.categoryPlot.renderer as BarRenderer
        renderer.defaultItemLabelGenerator = NonZeroCountLabelGenerator()
        renderer.defaultItemLabelsVisible = true
        renderer.defaultPositiveItemLabelPosition =
            ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER)
        renderer.defaultNegativeItemLabelPosition =
            ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER)

        return chart
    }

    fun save(chart: JFreeChart, path: String, width: Int = 900, height: Int = 600) {
        val file = File(path)
        file.parentFile?.mkdirs()
        ChartUtils.saveChartAsPNG(file, chart, width, height)
    }

    fun display(chart: JFreeChart, title: String = "Chart") {
        val frame = ChartFrame(title, chart)
        frame.pack()
        frame.isVisible = true
    }

    private class NonZeroCountLabelGenerator : StandardCategoryItemLabelGenerator() {
        override fun generateLabel(dataset: CategoryDataset, row: Int, column: Int): String? {
            val value = dataset.getValue(row, column)?.toInt() ?: 0
            return if (value == 0) null else value.toString()
        }
    }
}