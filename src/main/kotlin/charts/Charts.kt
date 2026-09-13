package charts

import model.Player
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.save
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.layers.bars
import org.jetbrains.kotlinx.kandy.letsplot.layers.pie
import java.awt.Desktop
import java.io.File
import org.jetbrains.kotlinx.kandy.letsplot.style.Style
import org.jetbrains.kotlinx.kandy.letsplot.tooltips.tooltips


fun buildPositionChart(players: List<Player>) {

    val total = players.size.toDouble()
    val counts = players.groupingBy { it.position.russianName }.eachCount()

    // Формируем подписи вида: "Полузащитник: 15 (30%)"
    val labelsWithPercent = counts.map { (position, count) ->
        val percent = String.format(java.util.Locale.US, "%.1f%%", (count / total) * 100)
        "$position ($percent)"
    }

    val chartData = mapOf(
        "Позиции" to labelsWithPercent,
        "counts" to counts.values.toList()
    )

    plot(chartData) {

        pie {
            slice("counts")
            fillColor("Позиции") // Привязываем нашу строку к цвету (движок её не удалит)
            size = 25.0

            tooltips {
                // Выводим только две строки: название (уже с процентом) и количество
                line("Позиция: @{Позиции}")
                line("Количество: @counts")
            }
        }

        layout {
            // Чтобы название было по центру
            title = "Доля конкретных позиций от всех игроков"
            size = 800 to 550
            style(Style.Void)
        }
    }.save("position.html")

    val file = File("lets-plot-images/position.html")
    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().browse(file.toURI())
    }
}

fun buildTop5TeamsChart(players: List<Player>) {

    // Получаем список названий 5 самых дорогих команд
    val top5TeamNames = players
        .groupBy { it.team.name }
        .toList()
        .sortedByDescending { (_, squad) -> squad.sumOf { it.transferCost } }
        .take(5)
        .map { it.first } // Оставляем только название

    // Отбираем игроков этих команд
    for (teamName in top5TeamNames) {
        val teamPlayers = players.filter { it.team.name == teamName }
        val counts = teamPlayers.groupingBy { it.position.russianName }.eachCount()

        val chartData = mapOf(
            "Позиция" to counts.keys.toList(),
            "Количество" to counts.values.toList()
        )

        // Заменяем пробелы в имени команды на "_", чтобы файлы сохранялись корректно
        val fileName = teamName.replace(" ", "_") + "_structure.html"

        plot(chartData) {
            bars {
                x("Позиция")
                y("Количество")
                fillColor("Позиция") // Автоматическая раскраска каждого столбца

                tooltips {
                    line("Позиция: @{Позиция}")
                    line("Игроков: @{Количество}")
                }
            }

            layout {
                title = "Структура состава команды: $teamName"
                size = 700 to 450
            }
        }.save(fileName)
    }

    val file = File("lets-plot-images/top5_teams.html")
    if (file.exists() && Desktop.isDesktopSupported()) {
        Desktop.getDesktop().browse(file.toURI())
    }
}