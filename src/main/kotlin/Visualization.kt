import model.Player
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.save
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.layers.bars
import org.jetbrains.kotlinx.kandy.letsplot.layers.points
import org.jetbrains.kotlinx.kandy.letsplot.feature.Position
import org.jetbrains.kotlinx.kandy.letsplot.feature.position
import org.jetbrains.kotlinx.kandy.letsplot.y

fun createAndSavePlots(players: List<Player>) {
    if (players.isEmpty()) return

    val maxVal = players.maxOf { it.marketValue }.toDouble()
    val binSize = if (maxVal > 0) maxVal / 10.0 else 1000000.0

    val binsMap = players.groupBy { player ->
        val binIndex = (player.marketValue.toDouble() / binSize).toInt().coerceAtMost(9)
        val startRange = String.format("%.1fM", (binIndex * binSize) / 1000000.0)
        startRange
    }.mapValues { it.value.size }

    val datasetBasic = mapOf(
        "range" to binsMap.keys.toList(),
        "count" to binsMap.values.toList()
    )

    val basicPlot = datasetBasic.plot {
        bars {
            x("range")
            y("count")
        }
        layout {
            title = "Распределение трансферной стоимости игроков (Интервалы)"
        }
    }
    basicPlot.save("distribution.html")
    println("График распределения сохранен в distribution.html")

    val datasetAdvanced = mapOf(
        "position" to players.map { it.position.russianName },
        "marketValue" to players.map { it.marketValue / 1_000_000.0 }
    )

    val advancedPlot = datasetAdvanced.plot {
        points {
            x("position")
            y("marketValue")
            color("position")

            position = Position.jitter()
            alpha = 0.6
        }

        y.axis.breaks(format = "{.1f}")

        layout {
            title = "Разброс трансферных стоимостей по позициям"
            yAxisLabel = "Рыночная стоимость (млн)"
            xAxisLabel = "Позиция игрока"
            size = 800 to 500
        }
    }

    advancedPlot.save("boxplot.html")
    println("График разброса сохранен в boxplot.html")
}