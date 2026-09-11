package visualisation

import model.Player
import org.jfree.chart.ChartFactory
import org.jfree.chart.JFreeChart
import org.jfree.data.general.DefaultPieDataset

class CountryChartCreator : ChartCreator {

    override fun create(players: List<Player>): JFreeChart {
        val dataset = DefaultPieDataset<String>()
        players
            .groupingBy { it.nationality }
            .eachCount()
            .forEach { (country, count) ->
                dataset.setValue(country, count)
            }

        return ChartFactory.createPieChart("Распределение игроков по странам", dataset, false, true, false)
    }
}