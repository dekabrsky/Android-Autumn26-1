import model.Player
import model.Position
import resolver.Resolver
import parser.CsvParser
import parser.ParseResult
import java.util.Locale
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.layers.bars
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.layers.points
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.util.color.Color
import org.jetbrains.kotlinx.kandy.letsplot.export.save


fun main() {
    val csvLines = object {}.javaClass.classLoader
        .getResourceAsStream("fakePlayersDirty.csv")!!
        .bufferedReader()
        .readLines()

    val parseResult = CsvParser.parse(csvLines)
    val players = parseResult.players
    val resolver = Resolver(players)

    printAllReports(players, resolver, parseResult)
    println()

    showTop10TeamsByTransferCost(players)
    println()

    showForwardsCostVsGoals(players)
}

fun printAllReports(
    players: List<Player>,
    resolver: Resolver,
    parseResult: ParseResult
) {
    println("Игроков успешно загружено: ${players.size}")
    println("Пропущено строк: ${parseResult.skippedCount}")
    println()

    println("1. Игроков без агентства: ${resolver.getCountWithoutAgency()}")
    println()

    val bestDefender = resolver.getBestScorerDefender()
    println("2. Защитник с наибольшим числом голов:")
    println("${bestDefender.first}: ${bestDefender.second}")
    println()

    println("3. Позиция самого дорогого немецкого игрока:")
    println(resolver.getTheExpensiveGermanPlayerPosition())
    println()

    val rudestTeam = resolver.getTheRudestTeam()
    println("4. Команда с наибольшим средним числом красных карточек:")
    println("${rudestTeam.name} (${rudestTeam.city})")
    println()

    println("5. Средняя трансферная стоимость по позициям (по убыванию):")
    resolver.getAverageTransferCostByPosition().forEach { (position, avgCost) ->
        println("${position.russianName}: $avgCost")
    }
    println()

    println("6. Топ-3 самых полезных игроков (голы + 2 * передачи):")
    resolver.getMostValuablePlayers().forEachIndexed { index, player ->
        println("${index + 1}. ${player.name} — ${player.goals + 2* player.assists}")
    }
    println()

    println("7. Самые популярные агентства по странам:")
    resolver.getMostPopularAgencyByCountry().forEach { (country, agency) ->
        println("$country: $agency")
    }
    println()

    println("8. Доля голов по медиане стоимости:")
    val (below, above) = resolver.getGoalsShareByMedianCost()
    println("Игроки дешевле или на уровне медианы: ${"%.2f%%".format(Locale.US, below * 100)}")
    println("Игроки дороже медианы: ${"%.2f%%".format(Locale.US, above * 100)}")
}

fun showTop10TeamsByTransferCost(players: List<Player>) {
    val teamCosts = players
        .groupBy { it.team.name }
        .mapValues { (_, teamPlayers) -> teamPlayers.sumOf { it.transferCost } }
        .entries
        .sortedByDescending { it.value }
        .take(10)

    val df = dataFrameOf(
        "Team" to teamCosts.map { it.key },
        "Cost" to teamCosts.map { it.value }
    )

    df.plot {
        bars {
            x("Team") {
                axis.name = "Команда"
            }
            y("Cost") {
                axis.name = "Суммарная трансферная стоимость"
            }
            fillColor("Team") { legend.name = "Команда" }
        }
        layout {
            title = "Топ-10 команд по трансферной стоимости состава"
            size = 1000 to 600
        }
    }.save("top10_teams_cost.png")
}

fun showForwardsCostVsGoals(players: List<Player>) {
    val forwards = players
        .filter { it.position == Position.FORWARD }
        .sortedBy { it.transferCost }

    if (forwards.isEmpty()) {
        println("Нападающие не найдены — график не построен.")
        return
    }

    val windowSize = 9
    val trend = forwards
        .windowed(windowSize)
        .map { window ->
            val avgCost = window.map { it.transferCost }.average()
            val avgGoals = window.map { it.goals }.average()
            avgCost to avgGoals
        }

    val trendCost = trend.map { it.first }
    val trendGoals = trend.map { it.second }

    val forwardPlot = forwards.toDataFrame().plot {
        points {
            x("transferCost") { axis.name = "Трансферная стоимость" }
            y("goals") { axis.name = "Голы" }

            color("yellowCards") {
                legend.name = "Жёлтые карточки"
            }

            size = 3.0
            alpha = 0.85
        }

        line {
            x(trendCost)
            y(trendGoals)
            color = Color.RED
        }

        layout {
            title = "Нападающие: стоимость vs голы"
            size = 1000 to 600
        }
    }

    forwardPlot.save("forwards_cost_vs_goals.png")
}
