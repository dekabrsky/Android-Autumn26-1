package visualization

import model.Player
import model.Position
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.save
import org.jetbrains.kotlinx.kandy.letsplot.layers.bars
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.letsplot.layers.points
import org.jetbrains.kotlinx.kandy.letsplot.x
import org.jetbrains.kotlinx.kandy.letsplot.y

fun generateVariant2Charts(players: List<Player>) {
    // 1. Базовая задача: Топ-10 команд по суммарной стоимости
    val top10Teams = players.groupBy { it.team.name }
        .mapValues { (_, teamPlayers) -> teamPlayers.sumOf { it.marketValue } }
        .toList()
        .sortedByDescending { it.second }
        .take(10)

    val basicData = mapOf(
        "team" to top10Teams.map { it.first },
        "value" to top10Teams.map { it.second / 1000000.0 }
    )

    plot(basicData) {
        x("team")
        y("value") {
            axis.name = "Стоимость (млн €)"
        }
        bars {}
    }.save("top10_teams.png")

    // 2. Продвинутая задача: Нападающие
    val attackers = players.filter { it.position == Position.FORWARD }
        .sortedBy { it.marketValue }

    val goals = attackers.map { it.goals }
    val windowSize = 5
    val movingAverage = goals.windowed(size = windowSize, step = 1, partialWindows = true) { it.average() }

    val advData = mapOf(
        "value" to attackers.map { it.marketValue / 1_000_000.0 },
        "goals" to goals,
        "yellowCards" to attackers.map { it.yellowCards },
        "trend" to movingAverage
    )

    plot(advData) {
        x("value") {
            axis.name = "Трансферная стоимость (млн €)"
        }
        y("goals") {
            axis.name = "Голы"
        }
        points {
            color("yellowCards")
            size = 3.5
        }
        line {
            y("trend")
        }
    }.save("attackers_value_vs_goals.png")
}