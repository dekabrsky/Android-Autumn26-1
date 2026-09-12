import charts.ChartBuilder
import filter.PlayerFilter
import filter.matches
import model.Position
import parser.CsvParser
import report.report
import resolver.Resolver
import java.util.Locale

fun main() {
    val parseResult = CsvParser.parseResource("fakePlayersDirty.csv")
    val players = parseResult.players

    println("=".repeat(60))

    println("Задание 1. Модель и маппинг")
    println("Успешно распознано игроков: ${players.size}")
    println("Пропущено «битых» строк: ${parseResult.skippedCount}")
    println()

    val resolver = Resolver(players)

    println("=".repeat(60))

    println("Задание 2. Запросы")

    println("1. Игроков без агентства: ${resolver.getCountWithoutAgency()}")

    val (bestDefenderName, bestDefenderGoals) = resolver.getBestScorerDefender()
    println("2. Лучший бомбардир среди защитников: $bestDefenderName ($bestDefenderGoals голов)")

    println("3. Позиция самого дорогого немецкого игрока: ${resolver.getTheExpensiveGermanPlayerPosition()}")

    val rudestTeam = resolver.getTheRudestTeam()
    println("4. Самая грубая команда: $rudestTeam (среднее число красных карточек: %.2f)".format(
        Locale.US, rudestTeam.averageRedCards
    ))

    println("5. Средняя трансферная стоимость по позициям (по убыванию):")
    resolver.getAverageTransferCostByPosition().forEach { (position, avgCost) ->
        println("   ${position.russianName}: %.2f".format(Locale.US, avgCost))
    }

    println("6. Топ-3 самых полезных игрока (голы + 2 * передачи):")
    resolver.getMostValuablePlayers().forEachIndexed { index, player ->
        println("   ${index + 1}. ${player.name} — полезность ${player.usefulness}")
    }

    println("7. Самое популярное агентство по странам:")
    resolver.getMostPopularAgencyByCountry().toSortedMap().forEach { (country, agency) ->
        println("   $country: $agency")
    }

    val (shareNotAboveMedian, shareAboveMedian) = resolver.getGoalsShareByMedianCost()
    println("8. Доля голов игроков не дороже медианы: %.2f%%, дороже медианы: %.2f%%".format(
        Locale.US, shareNotAboveMedian * 100, shareAboveMedian * 100
    ))
    println()

    println("=".repeat(60))

    println("Задание 3. Запросы")

    val streamingCandidate = CsvParser.findFirstOrNull("src/main/resources/fakePlayersDirty.csv") { player ->
        player.transferCost > 50_000_000 && player.participations >= 25 &&
            player.yellowCards == 0 && player.redCards == 0
    }
    println("10. Первый игрок дороже 50 000 000, с 25+ матчами и без карточек:")
    println("    ${streamingCandidate ?: "не найден"}")

    val cheapDefenders = resolver.countCheapDefenders(20_000_000)
    println("11. Защитников дешевле 20 000 000: $cheapDefenders")
    val cheapNonForwards = resolver.countNonForwardsCheaperThan(15_000_000)
    println("    Не-нападающих дешевле 15 000 000: $cheapNonForwards")
    val demoFilter = PlayerFilter.Or(
        PlayerFilter.ByPosition(Position.GOALKEEPER),
        PlayerFilter.CheaperThan(10_000_000)
    )
    println("    Вратарей или игроков дешевле 10 000 000: ${players.count { demoFilter.matches(it) }}")

    // п. 12 — мини-DSL отчёта
    println()
    println("12. Мини-DSL отчёт:")
    val textReport = report {
        metric("Без агентства") { players.count { it.agency == null } }
        metric("Всего игроков") { players.size }
        section("Атаки") {
            metric("Всего голов") { players.sumOf { it.goals } }
            metric("Всего голевых передач") { players.sumOf { it.assists } }
        }
        section("Дисциплина") {
            metric("Жёлтых карточек") { players.sumOf { it.yellowCards } }
            metric("Красных карточек") { players.sumOf { it.redCards } }
        }
    }
    print(textReport)
    println()

    println("=".repeat(60))

    println("Задание 4. Вариант 2 — визуализация")

    val topTeamsChart = ChartBuilder.buildTopTeamsByCostChart(players)
    ChartBuilder.saveChart(topTeamsChart, "charts/top10_teams_by_cost.png")
    ChartBuilder.showChart(topTeamsChart, "Топ-10 команд")
    println("Сохранён график: charts/top10_teams_by_cost.png")

    val forwardsChart = ChartBuilder.buildForwardsCostGoalsChart(players)
    ChartBuilder.saveChart(forwardsChart, "charts/forwards_cost_vs_goals.png")
    ChartBuilder.showChart(forwardsChart, "Стоимость vs Голы")
    println("Сохранён график: charts/forwards_cost_vs_goals.png")
}
