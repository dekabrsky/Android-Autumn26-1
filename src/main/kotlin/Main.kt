import parser.CsvParser
import parser.findFirstExpensiveFairPlayer
import report.report
import resolver.Resolver
import visualization.Charts
import java.io.File

fun main() {
    fun resource() = checkNotNull(CsvParser::class.java.getResourceAsStream("/fakePlayersDirty.csv")) {
        "Ресурс fakePlayersDirty.csv не найден"
    }.bufferedReader()
    val result = resource().use { CsvParser.parse(it) }
    val players = result.players
    val resolver = Resolver(players)
    println(report {
        metric("Загружено игроков") { players.size }
        metric("Пропущено строк") { result.skippedCount }
        section("Запросы") {
            metric("1. Без агентства") { resolver.getCountWithoutAgency() }
            metric("2. Лучший бомбардир-защитник") { resolver.getBestScorerDefender() }
            metric("3. Позиция самого дорогого немца") { resolver.getTheExpensiveGermanPlayerPosition() }
            metric("4. Команда с наибольшим средним числом удалений") {
                resolver.getTheRudestTeam().let { "${it.name} (${it.city}): ${it.averageRedCards}" }
            }
            section("5. Средняя стоимость по позициям") {
                resolver.getAverageTransferCostByPosition().forEach { (position, cost) ->
                    metric(position.russianName) { "%.2f".format(cost) }
                }
            }
            section("6. Топ-3 полезных игроков") {
                resolver.getMostValuablePlayers().forEach { player ->
                    metric(player.name) { player.goals.toLong() + 2L * player.assists }
                }
            }
            section("7. Популярные агентства по странам") {
                resolver.getMostPopularAgencyByCountry().forEach { (country, agency) -> metric(country) { agency } }
            }
            section("8. Доли голов по медианной стоимости") {
                metric("Медиана") { resolver.getMedianTransferCost() }
                val (cheaper, expensive) = resolver.getGoalsShareByMedianCost()
                metric("Не дороже медианы") { "%.2f%%".format(100 * cheaper) }
                metric("Дороже медианы") { "%.2f%%".format(100 * expensive) }
            }
        }
        metric("10. Первый дорогой игрок без карточек (потоковый поиск)") {
            findFirstExpensiveFairPlayer(resource())?.name ?: "Не найден"
        }
    })
    Charts.write(players, File("build/reports/charts")).forEach { println("График: ${it.absolutePath}") }
}
