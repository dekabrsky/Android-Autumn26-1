import chart.SquadCharts
import model.Player
import parser.CsvParser
import parser.ParseResult
import report.Report
import report.report
import resolver.IResolver
import resolver.Resolver
import java.io.File
import java.nio.charset.StandardCharsets

private const val DATA_RESOURCE = "fakePlayersDirty.csv"
private val CHARTS_DIR = File("build/charts")
private val REPORT_FILE = File("build/report.txt")

fun main() {
    // Диаграммы рисуются без окна, поэтому включаем headless-режим AWT
    System.setProperty("java.awt.headless", "true")

    val parsed = CsvParser.parseResource(DATA_RESOURCE)
    val resolver = Resolver(parsed.players)

    val text = championshipReport(parsed, resolver).render()
    print(text)

    // Консоль Windows может показывать кириллицу кракозябрами из-за кодировки
    // терминала, поэтому тот же отчёт дублируется в файл в UTF-8 —
    // его можно открыть в любом текстовом редакторе и сделать скриншот оттуда.
    REPORT_FILE.parentFile?.mkdirs()
    REPORT_FILE.writeText(text, StandardCharsets.UTF_8)

    val charts = SquadCharts.saveAll(parsed.players, CHARTS_DIR)
    println()
    println("Диаграммы сохранены:")
    charts.forEach { println("  ${it.absolutePath}") }
    println("Текстовый отчёт сохранён: ${REPORT_FILE.absolutePath}")
}

/**
 * Описание консольного отчёта на мини-DSL
 * Читается как структура отчёта, а не как последовательность println
 */
private fun championshipReport(parsed: ParseResult, resolver: IResolver): Report {
    val players: List<Player> = parsed.players

    return report("Чемпионат: отчёт по файлу $DATA_RESOURCE") {

        section("Данные") {
            metric("Строк с данными") { parsed.totalRows }
            metric("Разобрано игроков") { players.size }
            metric("Пропущено строк") { parsed.skippedCount }
            metric("Команд") { players.map { it.team }.distinct().size }
            metric("Стран") { players.map { it.country }.distinct().size }
        }

        section("1. Представительство") {
            metric("Игроков без агентства") { resolver.getCountWithoutAgency() }
            metric("Доля без агентства, %") {
                resolver.getCountWithoutAgency() * 100.0 / players.size
            }
        }

        section("2. Лучший бомбардир среди защитников") {
            metric("Игрок") { resolver.getBestScorerDefender().first }
            metric("Голов") { resolver.getBestScorerDefender().second }
        }

        section("3. Самый дорогой немецкий игрок") {
            metric("Позиция") { resolver.getTheExpensiveGermanPlayerPosition() }
        }

        section("4. Самая грубая команда") {
            metric("Команда") { resolver.getTheRudestTeam().title }
            metric("Красных карточек на игрока") {
                val team = resolver.getTheRudestTeam()
                val squad = players.filter { it.team == team }
                squad.sumOf { it.redCards }.toDouble() / squad.size
            }
        }

        section("5. Средняя трансферная стоимость по позициям") {
            resolver.getAverageTransferCostByPosition().forEach { (position, average) ->
                metric(position.russianName) { average }
            }
        }

        section("6. Топ-3 самых полезных игроков") {
            resolver.getMostValuablePlayers().forEachIndexed { index, player ->
                metric("${index + 1}. ${player.name}") {
                    "${player.usefulness} (голы ${player.goals}, передачи ${player.assists})"
                }
            }
        }

        section("7. Самое популярное агентство по странам") {
            resolver.getMostPopularAgencyByCountry().forEach { (country, agency) ->
                metric(country) { agency }
            }
        }

        section("8. Голы относительно медианной стоимости") {
            val (cheapShare, expensiveShare) = resolver.getGoalsShareByMedianCost()
            metric("Всего голов") { players.sumOf { it.goals } }
            metric("Доля голов не дороже медианы, %") { cheapShare * 100 }
            metric("Доля голов дороже медианы, %") { expensiveShare * 100 }
        }
    }
}
