package visualization

import model.Player
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.save
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.layers.bars
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.letsplot.layers.points
import org.jetbrains.kotlinx.kandy.util.color.Color

fun plotCountriesShare(players: List<Player>, outputPath: String = "countries_share.png") {
    val countryCounts: Map<String, Int> = players
        .groupBy { it.country }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }
        .toMap()

    val countries = countryCounts.keys.toList()
    val counts = countryCounts.values.toList()
    val total = counts.sum().toDouble()
    val sharesPercent = counts.map { it * 100.0 / total }

    val plot = plot {
        bars {
            x(countries) { axis.name = "Страна" }
            y(sharesPercent) { axis.name = "Доля игроков, %" }
            fillColor = Color.hex("#4C78A8")
        }
        layout {
            title = "Доля игроков по странам"
            size = 1000 to 550
        }
    }

    plot.save(outputPath)
}

fun plotTop10TeamsCumulativeValue(players: List<Player>, outputPath: String = "top10_teams_cumulative.png") {
    data class TeamValue(val teamName: String, val totalValue: Long)

    // Суммарная стоимость по командам, отсортированная по убыванию
    val teamTotals: List<TeamValue> = players
        .groupBy { it.team.name }
        .map { (name, group) -> TeamValue(name, group.sumOf { it.transferValue }) }
        .sortedByDescending { it.totalValue }

    val grandTotal = teamTotals.sumOf { it.totalValue }.toDouble()
    if (grandTotal <= 0.0) {
        // Нечего рисовать
        return
    }

    val top10 = teamTotals.take(10)

    // Накопленная доля: runningFold даёт [0, v1, v1+v2, ...], drop(1) убирает начальный 0
    val cumulativeValues = top10
        .map { it.totalValue }
        .runningFold(0L) { acc, value -> acc + value }
        .drop(1)
        .map { it * 100.0 / grandTotal }

    val teamNames = top10.map { it.teamName }
    val individualSharePercent = top10.map { it.totalValue * 100.0 / grandTotal }

    val plot = plot {
        // Столбцы — индивидуальная доля каждой команды
        bars {
            x(teamNames) { axis.name = "Команда" }
            y(individualSharePercent) { axis.name = "Доля / накопленная доля, %" }
            fillColor = Color.hex("#4C78A8")
            alpha = 0.85
        }
        // Линия накопленной доли
        line {
            x(teamNames)
            y(cumulativeValues)
            color = Color.hex("#E45756")
            width = 2.5
        }
        // Точки на линии для наглядности
        points {
            x(teamNames)
            y(cumulativeValues)
            color = Color.hex("#E45756")
            size = 5.0
        }
        layout {
            title = "Топ-10 команд по трансферной стоимости (диаграмма Парето)"
            subtitle = "Столбцы — доля команды, линия — накопленная доля от общей стоимости"
            size = 1100 to 600
        }
    }

    plot.save(outputPath)
}