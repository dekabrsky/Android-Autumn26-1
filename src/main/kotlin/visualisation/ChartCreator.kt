package visualisation

import model.Player
import org.jfree.chart.JFreeChart

interface ChartCreator {

    fun create(players: List<Player>): JFreeChart
}