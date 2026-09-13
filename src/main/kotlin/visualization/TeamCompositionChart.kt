package visualization

import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.category.DefaultCategoryDataset
import java.io.File

class TeamCompositionChart(private val composition: TopTeamsComposition) {

    fun buildChart(): JFreeChart {
        val dataset = DefaultCategoryDataset()

        composition.teams.forEach { team ->
            val positionCounts = composition.teamPositionCounts[team.name].orEmpty()

            composition.allPositions.forEach { positionName ->
                val count = positionCounts[positionName] ?: 0
                dataset.addValue(count, positionName, team.name)
            }
        }

        return ChartFactory.createStackedBarChart(
            "Структура составов топ-${composition.teams.size} команд по стоимости",
            "Команда",
            "Количество игроков",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        )
    }

    fun render(outputPath: String = "top_teams_composition.png", width: Int = 900, height: Int = 600) {
        val chart = buildChart()
        ChartUtils.saveChartAsPNG(File(outputPath), chart, width, height)
    }
}