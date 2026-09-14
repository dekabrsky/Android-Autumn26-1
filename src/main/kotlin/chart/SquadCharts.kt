package chart

import model.Player
import model.Position
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import org.jfree.chart.labels.StandardPieSectionLabelGenerator
import org.jfree.chart.plot.PiePlot
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.general.DefaultPieDataset
import java.io.File

/**
 * Задание 4, вариант 1 «Состав» базовая задача: доля игроков каждой позиции среди всех игроков
 * продвинутая: структура составов топ-5 команд по суммарной трансферной стоимости
 */
object SquadCharts {

    private const val DEFAULT_TOP_TEAMS = 5
    private const val PIE_WIDTH = 900
    private const val PIE_HEIGHT = 600
    private const val BAR_WIDTH = 1100
    private const val BAR_HEIGHT = 650

    /** Базовая задача: круговая диаграмма долей позиций */

    fun positionShare(players: List<Player>): JFreeChart {
        require(players.isNotEmpty()) { "Выборка игроков пуста" }

        val dataset = DefaultPieDataset<String>()
        players.groupingBy { it.position }.eachCount()
            .toList()
            .sortedByDescending { it.second }
            .forEach { (position, count) -> dataset.setValue(position.russianName, count.toDouble()) }

        val chart = ChartFactory.createPieChart(
            "Доля позиций среди всех игроков (${players.size})",
            dataset,
            true,
            true,
            false,
        )

        @Suppress("UNCHECKED_CAST")
        val plot = chart.plot as PiePlot<String>
        plot.labelGenerator = StandardPieSectionLabelGenerator("{0}: {1} ({2})")
        return chart
    }

    /**
     * составы топ-команд по суммарной трансферной стоимости,каждый столбец - команда, сегменты столбца - позиции игроков
     */
    fun topTeamsComposition(players: List<Player>, topCount: Int = DEFAULT_TOP_TEAMS): JFreeChart {
        require(players.isNotEmpty()) { "Выборка игроков пуста" }
        require(topCount > 0) { "topCount должен быть положительным" }

        val squads = players.groupBy { it.team }
        val topTeams = squads.entries
            .sortedByDescending { (_, squad) -> squad.sumOf { it.transferCost } }
            .take(topCount)

        val dataset = DefaultCategoryDataset()
        for ((team, squad) in topTeams) {
            val byPosition = squad.groupingBy { it.position }.eachCount()
            for (position in Position.entries) {
                dataset.addValue(
                    (byPosition[position] ?: 0).toDouble(),
                    position.russianName,
                    team.title,
                )
            }
        }

        return ChartFactory.createStackedBarChart(
            "Состав топ-$topCount команд по суммарной трансферной стоимости",
            "Команда",
            "Игроков в составе",
            dataset,
        )
    }


    fun saveAll(players: List<Player>, outputDir: File): List<File> {
        outputDir.mkdirs()

        val pie = File(outputDir, "positions-share.png")
        ChartUtils.saveChartAsPNG(pie, positionShare(players), PIE_WIDTH, PIE_HEIGHT)

        val bars = File(outputDir, "top5-teams-composition.png")
        ChartUtils.saveChartAsPNG(bars, topTeamsComposition(players), BAR_WIDTH, BAR_HEIGHT)

        return listOf(pie, bars)
    }
}
