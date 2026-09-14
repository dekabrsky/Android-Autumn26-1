import model.Player
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.general.DefaultPieDataset
import java.io.File

object Visual {
    fun playersByCountriesChart(players: List<Player>) {
        val dataset = DefaultPieDataset<String>()
        players.groupingBy { it.Nationality }
            .eachCount()
            .forEach { (country, count) -> dataset.setValue(country, count.toDouble() / players.size) }

        val chart = ChartFactory.createPieChart(
            "Доля игроков по странам",
            dataset,
            true,
            true,
            false
        )
        ChartUtils.saveChartAsPNG(File("1.png"), chart, 800, 600)
    }

    fun top10TeamsByTotalCost(players: List<Player>) {
        val teams = players.groupBy { it.Team }
            .map { (t, list) -> t to list.sumOf { it.TransferCost } }
            .sortedByDescending { it.second }
            .take(10)
        val totalCost = players.sumOf { it.TransferCost }.toDouble()
        if (totalCost == 0.0) return

        val costs = teams.map { it.second.toDouble() }
        val cumulativeShares = costs.runningFold(0.0, Double::plus)
            .drop(1)
            .map { it / totalCost * 100.0 }

        val dataset = DefaultCategoryDataset()
        teams.zip(cumulativeShares)
            .forEach { (team, cumulativeShare) ->
                dataset.addValue(team.second / 1000000.0, "Стоимость команды, млн", team.first.Name)
                dataset.addValue(cumulativeShare, "Накопленная доля, %", team.first.Name)
            }

        val chart = ChartFactory.createBarChart(
            "Топ-10 команд и накопленная доля стоимости",
            "Команда",
            "Значение",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        )

        ChartUtils.saveChartAsPNG(File("2.png"), chart, 800, 600)
    }
}