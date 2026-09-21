import parser.CsvParser
import parser.StreamingPlayerSearch
import report.report
import resolver.Resolver
import visualization.TransferCostCharts
import java.nio.file.Path
import java.util.Locale

fun main() {
    val classLoader = Thread.currentThread().contextClassLoader
    val parseResult = classLoader.getResourceAsStream("fakePlayersDirty.csv")?.use(CsvParser::parse)
        ?: error("Ресурс fakePlayersDirty.csv не найден")
    val players = parseResult.players
    val resolver = Resolver(players)

    println(report {
        metric("Успешно прочитано строк") { players.size }
        metric("Пропущено строк") { parseResult.skippedCount }
        section("Обязательные запросы") {
            metric("Игроков без агентства") { resolver.getCountWithoutAgency() }
            metric("Лучший защитник") { resolver.getBestScorerDefender() }
            metric("Позиция самого дорогого немецкого игрока") {
                resolver.getTheExpensiveGermanPlayerPosition()
            }
            metric("Команда с максимальным средним числом удалений") {
                resolver.getTheRudestTeam().name
            }
            metric("Средняя стоимость по позициям") {
                resolver.getAverageTransferCostByPosition().mapValues { (_, cost) -> formatMoney(cost) }
            }
            metric("Топ-3 полезных игроков") {
                resolver.getMostValuablePlayers().joinToString { it.name }
            }
            metric("Популярные агентства по странам") {
                resolver.getMostPopularAgencyByCountry()
            }
            metric("Доли голов относительно медианы") {
                resolver.getGoalsShareByMedianCost().let { (lower, upper) ->
                    "не дороже — ${formatPercent(lower)}, дороже — ${formatPercent(upper)}"
                }
            }
        }
        section("Командные агрегаты") {
            val mostExpensiveTeam = players.map { it.team }.distinct().maxBy { it.totalTransferCost }
            metric("Самый дорогой состав") {
                "${mostExpensiveTeam.name}: ${formatMoney(mostExpensiveTeam.totalTransferCost.toDouble())}"
            }
        }
    })

    val streamedPlayer = classLoader.getResourceAsStream("fakePlayersDirty.csv")?.use {
        StreamingPlayerSearch.findFirstEligible(it)
    }
    println("Первый подходящий игрок при потоковом чтении: ${streamedPlayer?.name ?: "не найден"}")

    val chartPaths = TransferCostCharts.saveAll(players, Path.of("build", "charts"))
    println("Графики сохранены:")
    chartPaths.forEach { println("  ${it.toAbsolutePath()}") }
}

private fun formatMoney(value: Double): String = String.format(Locale.US, "%,.2f", value)

private fun formatPercent(value: Double): String = String.format(Locale.US, "%.2f%%", value * 100)
