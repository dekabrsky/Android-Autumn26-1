import filter.PlayerFilter
import model.Position
import parser.CsvParser
import report.report
import resolver.Resolver
import visualization.calculateCountryShares
import visualization.calculateTopTeamCostShares
import visualization.saveCountryShareChart
import visualization.saveTeamCostCumulativeShareChart
import java.io.File
import java.text.DecimalFormat

fun main() {
    val lines = readResourceLines("fakePlayersDirty.csv")

    val parseResult = CsvParser.parse(lines)
    val players = parseResult.players
    val resolver = Resolver(players)
    val chartsDir = File("charts")
    val countryChartFile = chartsDir.resolve("country-share.png")
    val teamCostChartFile = chartsDir.resolve("team-cost-cumulative-share.png")
    val countryShares = calculateCountryShares(players)
    val teamCostShares = calculateTopTeamCostShares(players)

    saveCountryShareChart(
        countryShares = countryShares,
        file = countryChartFile,
    )
    saveTeamCostCumulativeShareChart(
        teamCostShares = teamCostShares,
        file = teamCostChartFile,
    )

    val firstStreamingMatch = findFirstExpensiveExperiencedPlayerWithoutCards()
    val expensiveDefenderFilter = PlayerFilter.And(
        PlayerFilter.ByPosition(Position.DEFENDER),
        PlayerFilter.Not(PlayerFilter.CheaperThan(50_000_000)),
    )

    println(
        report {
            section("Задание 1. Модель и маппинг") {
                metric("Файл") { "fakePlayersDirty.csv" }
                metric("Игроков загружено") { players.size }
                metric("Строк пропущено") { parseResult.skippedCount }
            }

            section("Задание 2. Запросы") {
                metric("1. Без агентства") { resolver.getCountWithoutAgency() }
                metric("2. Лучший защитник-бомбардир") {
                    val (name, goals) = resolver.getBestScorerDefender()
                    "$name, голов: $goals"
                }
                metric("3. Позиция самого дорогого немца") { resolver.getTheExpensiveGermanPlayerPosition() }
                metric("4. Самая грубая команда") {
                    val team = resolver.getTheRudestTeam()
                    "${team.name} (${team.city}), красных карточек в среднем: ${formatNumber(resolver.getAverageRedCards(team))}"
                }
                metric("5. Средняя стоимость по позициям") {
                    resolver.getAverageTransferCostByPosition()
                        .entries
                        .joinToString { (position, averageCost) -> "${position.positionName}: ${formatMoney(averageCost)}" }
                }
                metric("6. Топ-3 полезных игрока") {
                    resolver.getMostValuablePlayers()
                        .joinToString { player -> "${player.name} (${player.goals + 2 * player.assists})" }
                }
                metric("7. Популярные агентства по странам") {
                    resolver.getMostPopularAgencyByCountry()
                        .entries
                        .sortedBy { it.key }
                        .joinToString { (country, agency) -> "$country: $agency" }
                }
                metric("8. Доля голов не дороже/дороже медианы") {
                    val (cheaperShare, expensiveShare) = resolver.getGoalsShareByMedianCost()
                    "${formatPercent(cheaperShare)} / ${formatPercent(expensiveShare)}"
                }
            }

            section("Задание 3. Дополнительно") {
                metric("9. Стоимость самой грубой команды") { formatMoney(resolver.getTotalTransferCost(resolver.getTheRudestTeam())) }
                metric("10. Первый дорогой опытный игрок без карточек") {
                    firstStreamingMatch?.let { "${it.name}, ${it.teamName}, ${formatMoney(it.transferCost)}" } ?: "не найден"
                }
                metric("11. Защитников стоимостью от 50 млн") { players.count(expensiveDefenderFilter::matches) }
                metric("12. Мини-DSL отчета") { "этот вывод построен через report { ... }" }
            }

            section("Задание 4. Визуализация, вариант 4") {
                metric("Стран в данных") { countryShares.size }
                metric("Лидер по числу игроков") {
                    countryShares.firstOrNull()?.let { "${it.country}: ${it.playersCount} (${formatPercent(it.share)})" } ?: "нет данных"
                }
                metric("Команд в топе") { teamCostShares.size }
                metric("Суммарная доля стоимости топ-10") {
                    teamCostShares.lastOrNull()?.let { formatPercent(it.cumulativeShare) } ?: "нет данных"
                }
                metric("График стран") { countryChartFile.path }
                metric("График стоимости команд") { teamCostChartFile.path }
            }

            section("Задание 5. Тесты") {
                metric("Команда для проверки") { ".\\gradlew.bat clean test" }
            }
        },
    )
}

private fun readResourceLines(name: String): List<String> =
    requireNotNull(Thread.currentThread().contextClassLoader.getResourceAsStream(name)) {
        "Resource $name was not found"
    }.bufferedReader().use { it.readLines() }

private fun findFirstExpensiveExperiencedPlayerWithoutCards() =
    requireNotNull(Thread.currentThread().contextClassLoader.getResourceAsStream("fakePlayersDirty.csv")) {
        "Resource fakePlayersDirty.csv was not found"
    }.bufferedReader().useLines { lines ->
        CsvParser.findFirstExpensiveExperiencedPlayerWithoutCards(lines)
    }

private fun formatMoney(value: Number): String = "${formatNumber(value.toDouble() / 1_000_000)} млн"

private fun formatPercent(value: Double): String = "${formatNumber(value * 100)}%"

private fun formatNumber(value: Double): String = DecimalFormat("0.#").format(value)
