package grath

import model.Player
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.save
import org.jetbrains.kotlinx.kandy.letsplot.layers.bars
import org.jetbrains.kotlinx.kandy.util.color.Color
import java.io.File
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout

fun buildTopTeamsChart(players: List<Player>, n: Int = 10) =
    topTeamsByTotalValue(players, n).let { data ->
        plot {
            bars {
                x(data.map { (team, _) -> team })
                y(data.map { (_, value) -> value })
                fillColor = Color.BLUE
            }
            layout {
                title = "Топ-$n команд по суммарной трансферной стоимости"
                size = 1200 to 700
            }
        }
    }

fun saveTopTeamsChart(players: List<Player>, path: File, n: Int = 10) {
    buildTopTeamsChart(players, n).save(path.absolutePath, dpi = 150)
}