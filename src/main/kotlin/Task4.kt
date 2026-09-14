package graph

import model.Player
import model.Position
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.general.DefaultPieDataset
import java.io.File

fun main() {

    val inputStream = object {}.javaClass.getResourceAsStream("/fakePlayers.csv")

    val players = parser.readCSV(inputStream).players

    createPositionShareChart(players)
    createTopTeamsStructureChart(players)

    println("Gatova")
}

// 1 Доля игроков по позициям

fun createPositionShareChart(players: List<Player>) {

    val dataset = DefaultPieDataset<String>()

    val totalPlayers = players.size

    val positionCounts = players
        .groupingBy { it.position }
        .eachCount()

    positionCounts.forEach { (position, count) ->

        val percentage = count.toDouble() / totalPlayers * 100

        dataset.setValue(
            position.className,
            percentage
        )
    }

    val chart: JFreeChart = ChartFactory.createPieChart(
        "Доля игроков по позициям",
        dataset,
        true,
        true,
        false
    )

    saveChart(
        chart = chart,
        fileName = "players_by_position.png"
    )
}

// 2 Структура топ-5 команд

fun createTopTeamsStructureChart(players: List<Player>) {
    val topTeams = players
        .groupBy { it.team }
        .mapValues { (_, teamPlayers) ->
            teamPlayers.sumOf { it.transferCost }
        }
        .entries
        .sortedByDescending { it.value }
        .take(5)
        .map { it.key }

    val dataset = DefaultCategoryDataset()

    topTeams.forEach { team ->

        val teamPlayers = players
            .filter { it.team == team }

        val playersByPosition = teamPlayers
            .groupingBy { it.position }
            .eachCount()

        playersByPosition.forEach { (position, count) ->

            dataset.addValue(
                count,
                position.className,
                team.name
            )
        }
    }


    val chart: JFreeChart = ChartFactory.createStackedBarChart(
        "Структура составов топ-5 команд",
        "Команда",
        "Количество игроков",
        dataset
    )

    saveChart(
        chart = chart,
        fileName = "top5_teams_structure.png"
    )
}

// Сохранение графика

fun saveChart(
    chart: JFreeChart,
    fileName: String
) {

    val outputDirectory = File("charts")

    if (!outputDirectory.exists()) {
        outputDirectory.mkdirs()
    }

    val file = File(
        outputDirectory,
        fileName
    )

    ChartUtils.saveChartAsPNG(
        file,
        chart,
        1000,
        700
    )
}