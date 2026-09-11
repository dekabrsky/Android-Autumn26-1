package visualisation

import model.Player
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import java.io.File

class VisualisationService(
    private val teamTransferChartCreator: TeamTransferChartCreator = TeamTransferChartCreator(),
    private val countryChartCreator: CountryChartCreator = CountryChartCreator()
) {

    fun createTopTeamsTransferChart(players: List<Player>) {
        val chart = teamTransferChartCreator.create(players)
        saveChartFile(chart, "top_10_teams_transfer_cost.png", 1200, 800)
    }

    fun createCountryChart(players: List<Player>) {
        val chart = countryChartCreator.create(players)
        saveChartFile(chart, "players_by_country.png", 800, 600)
    }

    fun saveChartFile(chart: JFreeChart, fileName: String, width: Int, height: Int) {
        val file = File(fileName)
        ChartUtils.saveChartAsPNG(file, chart, width, height)
        println("Файл ${file.name} был сохранен в ${file.absolutePath}")
    }
}