package grath

import model.Player
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.save
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.letsplot.layers.points
import org.jetbrains.kotlinx.kandy.util.color.Color
import java.io.File
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout

fun buildStrikersTrendChart(players: List<Player>, window: Int = 7) =
    run {
        val pointsData = strikersPoints(players)
        val trendData = movingAverageTrend(pointsData, window)

        plot {
            points {
                x(pointsData.map { it.value })
                y(pointsData.map { it.goals })
                color(pointsData.map { it.yellowCards })
                size = 3.5
                alpha = 0.7
            }
            line {
                x(trendData.map { it.first })
                y(trendData.map { it.second })
                color = Color.RED
                width = 1.5
            }
            layout {
                title = "Нападающие: стоимость vs голы"
                subtitle = "Тренд — скользящее среднее (окно=$window)"
                size = 1200 to 700
            }
        }
    }

fun saveStrikersTrendChart(players: List<Player>, path: File, window: Int = 7) {
    buildStrikersTrendChart(players, window).save(path.absolutePath, dpi = 150)
}