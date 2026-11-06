import chart.Charts
import filter.PlayerFilter
import filter.and
import filter.bestDefenderScorer
import filter.select
import model.Position
import parser.CsvParser
import report.report
import resolver.Resolver
import task.findFirstCleanStar
import java.io.File

fun main() {
    System.setProperty("java.awt.headless", "true")

    val (players, skipped) = CsvParser.parseResource("fakePlayersDirty.csv")
    println("Загружено игроков: ${players.size}, пропущено строк: $skipped\n")

    val resolver = Resolver(players)
    println("== Задание 2 ==")
    println("1. Без агентства: ${resolver.getCountWithoutAgency()}")
    println("2. Лучший бомбардир-защитник: ${resolver.getBestScorerDefender()}")
    println("3. Позиция самого дорогого немца: ${resolver.getTheExpensiveGermanPlayerPosition()}")
    val rude = resolver.getTheRudestTeam()
    println("4. Самая грубая команда: ${rude.name} (${"%.2f".format(rude.averageRedCards)} красных на игрока)")
    println("5. Средняя стоимость по позициям:")
    resolver.getAverageTransferCostByPosition().forEach { (pos, avg) -> println("   ${pos.russianName}: ${"%.0f".format(avg)}") }
    println("6. Топ-3 полезных:")
    resolver.getMostValuablePlayers().forEach { println("   ${it.name} (${it.utility})") }
    println("7. Популярное агентство по странам:")
    resolver.getMostPopularAgencyByCountry().toSortedMap().forEach { (c, a) -> println("   $c: $a") }
    val (cheap, expensive) = resolver.getGoalsShareByMedianCost()
    println("8. Доля голов: не дороже медианы %.1f%%, дороже %.1f%%".format(cheap * 100, expensive * 100))

    println("\n== Задание 3.10: потоковое чтение ==")
    println(findFirstCleanStar("fakePlayersDirty.csv") ?: "Не найден")

    println("\n== Задание 3.11: фильтры ==")
    println("Лучший защитник-бомбардир: ${bestDefenderScorer(players)}")
    val cheapForwards = players.select(PlayerFilter.ByPosition(Position.FORWARD) and PlayerFilter.CheaperThan(20_000_000))
    println("Нападающих дешевле 20 млн: ${cheapForwards.size}")

    println("\n== Задание 3.12: отчёт ==")
    println(
        report(players) {
            metric("Без агентства") { players.count { it.agency == null } }
            section("Атаки") {
                metric("Всего голов") { players.sumOf { it.goals } }
                metric("Всего передач") { players.sumOf { it.assists } }
            }
            section("Дисциплина") {
                metric("Жёлтых карточек") { players.sumOf { it.yellowCards } }
                metric("Красных карточек") { players.sumOf { it.redCards } }
            }
        }.render()
    )

    println("== Задание 4 (вариант 4): графики ==")
    Charts.save(Charts.countryShare(players), File("charts/country_share.png"))
    Charts.save(Charts.topTeamsPareto(players), File("charts/top_teams_pareto.png"), 1100, 700)
    println("Сохранено в папку charts/")
}
