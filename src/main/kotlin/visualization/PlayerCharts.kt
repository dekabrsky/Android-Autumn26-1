package visualization

import model.Player
import model.Position
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.labels.StandardPieSectionLabelGenerator
import org.jfree.chart.plot.CategoryPlot
import org.jfree.chart.plot.PiePlot
import org.jfree.chart.renderer.category.StackedBarRenderer
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.general.DefaultPieDataset
import java.awt.Color
import java.awt.Font
import java.io.File
import java.text.DecimalFormat

object PlayerCharts {
    fun savePositionShareChart(players: List<Player>, outputFile: File): File {
        outputFile.parentFile?.mkdirs()

        val dataset = DefaultPieDataset<String>()
        players.groupingBy { it.position }
            .eachCount()
            .forEach { (position, count) ->
                dataset.setValue(position.russianName, count)
            }

        val chart = ChartFactory.createPieChart(
            "Доля игроков по позициям",
            dataset,
            true,
            true,
            false,
        )

        @Suppress("UNCHECKED_CAST")
        val plot = chart.plot as PiePlot<String>
        plot.labelGenerator = StandardPieSectionLabelGenerator(
            "{0}: {1} ({2})",
            DecimalFormat("0"),
            DecimalFormat("0.0%"),
        )
        plot.backgroundPaint = Color.WHITE
        plot.labelFont = Font("SansSerif", Font.PLAIN, 12)
        chart.backgroundPaint = Color.WHITE

        ChartUtils.saveChartAsPNG(outputFile, chart, 1000, 700)
        return outputFile
    }

    fun saveTopTeamCompositionChart(players: List<Player>, outputFile: File): File {
        outputFile.parentFile?.mkdirs()

        val topTeams = players.groupBy { it.team }
            .mapValues { (_, teamPlayers) -> teamPlayers.sumOf { it.transferCost } }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
            .map { it.first }

        val playersByTeam = players.groupBy { it.team }
        val dataset = DefaultCategoryDataset()

        topTeams.forEach { team ->
            val countsByPosition = playersByTeam.getValue(team)
                .groupingBy { it.position }
                .eachCount()

            Position.entries.forEach { position ->
                dataset.addValue(
                    countsByPosition[position] ?: 0,
                    position.russianName,
                    team.name,
                )
            }
        }

        val chart = ChartFactory.createStackedBarChart(
            "Состав топ-5 команд по суммарной трансферной стоимости",
            "Команда",
            "Количество игроков",
            dataset,
        )

        chart.backgroundPaint = Color.WHITE
        val plot = chart.plot as CategoryPlot
        plot.backgroundPaint = Color.WHITE
        plot.rangeGridlinePaint = Color.LIGHT_GRAY
        plot.renderer = StackedBarRenderer().apply {
            setDefaultItemLabelsVisible(true)
        }
        plot.domainAxis.tickLabelFont = Font("SansSerif", Font.PLAIN, 11)
        plot.domainAxis.categoryLabelPositions =
            org.jfree.chart.axis.CategoryLabelPositions.UP_45

        ChartUtils.saveChartAsPNG(outputFile, chart, 1200, 800)
        return outputFile
    }
}
