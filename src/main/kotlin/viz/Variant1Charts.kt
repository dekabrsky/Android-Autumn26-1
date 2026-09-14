package viz

import model.Player
import model.Position
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.general.DefaultPieDataset
import java.io.File

object Variant1Charts {

    /**
     * Базовая задача варианта 1:
     * круговая диаграмма — доли позиций среди всех игроков.
     */
    fun savePositionPie(players: List<Player>, output: File) {
        val dataset = DefaultPieDataset<String>()
        players.groupingBy { it.position }
            .eachCount()
            .forEach { (pos, count) -> dataset.setValue(pos.russianName, count) }

        val chart = ChartFactory.createPieChart(
            "Распределение игроков по позициям",
            dataset,
            true,   // легенда
            true,   // tooltips
            false   // URL
        )
        ChartUtils.saveChartAsPNG(output, chart, 800, 600)
    }

    /**
     * Продвинутая задача варианта 1:
     * стек-бар: топ-5 команд по суммарной стоимости, разбитые по позициям.
     */
    fun saveTop5TeamsStacked(players: List<Player>, output: File) {
        // 1) Топ-5 команд по суммарной стоимости
        val top5 = players
            .groupBy { it.team }
            .mapValues { (_, list) -> list.sumOf { it.transferCost } }
            .entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key }

        // 2) Для каждой команды — сумма стоимости по позициям
        val dataset = DefaultCategoryDataset()
        for (team in top5) {
            val byPos = players.filter { it.team == team }.groupBy { it.position }
            for (pos in Position.entries) {
                val sum = byPos[pos]?.sumOf { it.transferCost } ?: 0L
                dataset.addValue(sum / 1_000_000.0, pos.russianName, team.name)
            }
        }

        val chart = ChartFactory.createStackedBarChart(
            "Топ-5 команд по стоимости состава (млн)",
            "Команда",
            "Стоимость, млн",
            dataset,
            PlotOrientation.VERTICAL,
            true, true, false
        )
        ChartUtils.saveChartAsPNG(output, chart, 1000, 700)
    }
}