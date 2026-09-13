import dsl.report
import parser.CsvParser
import resolver.Resolver
import java.io.File
import kotlin.io.useLines

fun main() {
    val parser = CsvParser()
    val file = File("src/main/resources/fakePlayersDirty.csv")

    val (players, skipped) = file.useLines { lines -> parser.parse(lines) }
    println("Успешно загружено: ${players.size}, пропущено кривых строк: $skipped\n")

    if (players.isEmpty()) return

    val resolver = Resolver(players)

    report(players) {
        metric("Пропущено битых строк при парсинге") { skipped }
        metric("Игроков без агентства") { resolver.getCountWithoutAgency() }

        section("Защита") {
            val (defender, goals) = resolver.getBestScorerDefender()
            metric("Лучший защитник-бомбардир") { "$defender ($goals голов)" }
            metric("Самая грубая команда") { resolver.getTheRudestTeam().name }
        }

        section("Стоимость") {
            metric("Позиция самого дорогого немца") { resolver.getTheExpensiveGermanPlayerPosition() }

            section("Средняя стоимость по позициям") {
                resolver.getAverageTransferCostByPosition().forEach { (position, avgCost) ->
                    metric(position.russianName) { String.format("%,.2f", avgCost) }
                }
            }

            section("Доля голов по медианной стоимости") {
                val (cheap, exp) = resolver.getGoalsShareByMedianCost()
                metric("Доля голов недорогих игроков") { String.format("%.1f%%", cheap * 100) }
                metric("Доля голов дорогих игроков") { String.format("%.1f%%", exp * 100) }
            }
        }

        section("Полезность") {
            metric("Топ-3 MVP") {
                resolver.getMostValuablePlayers().joinToString { it.name }
            }
        }
    }

    createAndSavePlots(players)
}