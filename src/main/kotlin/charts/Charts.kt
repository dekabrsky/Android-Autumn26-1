package charts

import model.Player
import model.Position
import model.Team
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.general.DefaultPieDataset
import java.awt.Color
import java.io.File

object Charts {

    fun saveAll(players: List<Player>, outputDir: File = File("charts")) {
        outputDir.mkdirs()
        savePositionShare(players, File(outputDir, "position_share.png"))
        saveTopTeamsComposition(players, File(outputDir, "top5_teams_composition.png"))
        println("Графики сохранены в папку: ${outputDir.absolutePath}")
    }

    // Базовая: доля игроков каждой позиции
    fun savePositionShare(players: List<Player>, file: File) {
        val dataset = DefaultPieDataset<String>()
        val total = players.size.toDouble()

        Position.entries.forEach { pos ->
            val count = players.count { it.position == pos }
            val share = count / total * 100
            dataset.setValue("${pos.russianName} (${"%.1f".format(share)}%)", count)
        }

        val chart = ChartFactory.createPieChart(
            "Доля игроков по позициям",
            dataset,
            true,
            true,
            false
        )
        chart.backgroundPaint = Color.WHITE

        ChartUtils.saveChartAsPNG(file, chart, 800, 600)
    }

    // Продвинутая: структура составов топ-5 команд по суммарной стоимости
    fun saveTopTeamsComposition(players: List<Player>, file: File) {
        val teams = players
            .groupBy { it.team.name to it.team.city }
            .map { (key, teamPlayers) -> Team(key.first, key.second, teamPlayers) }
            .sortedByDescending { it.totalTransferCost }
            .take(5)

        val dataset = DefaultCategoryDataset()
        for (team in teams) {
            for (pos in Position.entries) {
                val count = team.players.count { it.position == pos }
                dataset.addValue(count, pos.russianName, team.name)
            }
        }

        val chart = ChartFactory.createStackedBarChart(
            "Топ-5 команд: состав по позициям",
            "Команда",
            "Число игроков",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        )
        chart.backgroundPaint = Color.WHITE

        ChartUtils.saveChartAsPNG(file, chart, 1000, 600)
    }
}
