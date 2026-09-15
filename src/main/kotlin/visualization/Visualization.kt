package visualization

import model.Player
import model.Team
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.save
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.layers.bars
import org.jetbrains.kotlinx.kandy.letsplot.layers.pie

fun visualizePositionShares(players: List<Player>) {
    val positionCounts = players.groupBy { it.position }
        .mapValues { it.value.size }

    val data = mapOf(
        "Позиция" to positionCounts.keys.map { it.displayName },
        "Количество" to positionCounts.values.toList()
    )

    val p = plot(data) {
        layout {
            title = "Доля игроков по позициям на поле"
        }
        pie {
            slice("Количество")
            fillColor("Позиция")
        }
    }

    p.save("position_shares.png")
}

fun visualizeTopTeamsStructure(players: List<Player>) {
    val allTeams = players.groupBy { it.team.name }
        .map { (teamName, teamPlayers) -> Team(name = teamName, city = teamPlayers[0].team.city, players = teamPlayers) }

    val top5Teams = allTeams.sortedByDescending { it.totalCost }.take(5)
    val top5Names = top5Teams.map { it.name }.toSet()
    val top5Players = players.filter { it.team.name in top5Names }

    val groupedData = top5Players.groupBy { it.team.name to it.position }
        .map { (key, playerList) ->
            mapOf(
                "Команда" to key.first,
                "Позиция" to key.second.displayName,
                "Стоимость" to playerList.sumOf { it.transferCost.toLong() } / 1_000_000.0
            )
        }

    val chartData = mapOf(
        "Команда" to groupedData.map { it["Команда"] as String },
        "Позиция" to groupedData.map { it["Позиция"] as String },
        "Стоимость" to groupedData.map { it["Стоимость"] as Double }
    )

    val p = plot(chartData) {
        layout {
            title = "Структура стоимости составов ТОП-5 команд"
        }
        // Функция bars теперь тоже на месте!
        bars {
            x("Команда")
            y("Стоимость")
            fillColor("Позиция") // Автоматически группирует столбцы в Stacked Bar
        }
    }

    p.save("top_5_teams_structure.png")
}