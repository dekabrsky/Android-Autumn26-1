import filter.matches
import parser.CsvParser
import report.report
import resolver.Resolver
import visualization.PositionDistribution
import visualization.PositionShareChart
import visualization.TeamCompositionChart
import visualization.TopTeamsComposition

fun main() {
    print("Yeah rock!")

    println()
    println("ЗАДАНИЕ 1")

    val parseResultDirty =
        CsvParser.parseResource("fakePlayersDirty.csv")

    println()
    println("Результаты парсинга fakePlayersDirty.csv")
    println("Количество игроков: ${parseResultDirty.players.size}")
    println("Количество пропусков: ${parseResultDirty.skippedCount}")

    println()
    println("ЗАДАНИЕ 2")

    val resolver = Resolver(parseResultDirty.players)

    println()
    println("1. Количество игроков без агентства:")
    println(resolver.getCountWithoutAgency())

    println()
    println("2. Лучший бомбардир среди защитников:")
    val (defenderName, defenderGoals) = resolver.getBestScorerDefender()
    println("$defenderName — $defenderGoals гол(ов)")

    println()
    println("3. Позиция самого дорогого немецкого игрока:")
    println(resolver.getTheExpensiveGermanPlayerPosition())

    println()
    println("4. Самая грубая команда (по среднему числу красных карточек на игрока):")
    val rudestTeam = resolver.getTheRudestTeam()
    println("${rudestTeam.name} (${rudestTeam.city})")

    println()
    println("5. Средняя трансферная стоимость по позициям (по убыванию):")
    resolver.getAverageTransferCostByPosition().forEach { (position, avgCost) ->
        println("${position.positionName}: %.2f".format(avgCost))
    }

    println()
    println("6. Топ-3 самых полезных игрока:")
    resolver.getMostValuablePlayers().forEachIndexed { index, player ->
        val usefulness = player.goals + 2 * player.assists
        println("${index + 1}. ${player.name} — полезность: $usefulness (голы: ${player.goals}, передачи: ${player.assists})")
    }

    println()
    println("7. Самое популярное агентство по странам:")
    resolver.getMostPopularAgencyByCountry().forEach { (country, agency) ->
        println("$country: $agency")
    }

    println()
    println("8. Доля голов по медианной трансферной стоимости:")
    val (cheapShare, expensiveShare) = resolver.getGoalsShareByMedianCost()
    println("Игроки не дороже медианы: %.2f%%".format(cheapShare * 100))
    println("Игроки дороже медианы: %.2f%%".format(expensiveShare * 100))

    println()

    println("ЗАДАНИЕ 3")
    println("9. Команда-агрегат:")

    val teams = parseResultDirty.players.map { it.team }.distinct()
    println("Всего команд: ${teams.size}")

    teams.sortedByDescending { it.totalTransferCost }.forEachIndexed { index, team ->
        println("${index + 1}. ${team.name} (игроков в составе: ${team.players.size})")
        println("   Стоимость состава: ${team.totalTransferCost}")
        println("   Среднее число красных карточек: ${"%.2f".format(team.averageRedCards)}")
    }

    println()
    println("10. Первый игрок ≥50 000 000, ≥25 матчей, без единой карточки (потоковое чтение):")

    val player = CsvParser.findFirstDisciplinedHighValuePlayer("fakePlayers.csv")

    if (player != null) {
        println("${player.name} (${player.team.name}) — стоимость: ${player.transferCost}, " +
                "матчей: ${player.participations}, жёлтые: ${player.yellowCards}, красные: ${player.redCards}")
    } else {
        println("Подходящий игрок не найден")
    }

    println()
    println("11 (доп). Проверка фильтров — защитники через PlayerFilter:")

    val defenderFilter: filter.PlayerFilter = filter.PlayerFilter.ByPosition(model.Position.DEFENDER)
    val cheapFilter: filter.PlayerFilter = filter.PlayerFilter.CheaperThan(30_000_000L)
    val combinedFilter = filter.PlayerFilter.And(defenderFilter, cheapFilter)

    val cheapDefendersCount = parseResultDirty.players.count { combinedFilter.matches(it) }
    println("Недорогих защитников (< 30 000 000): $cheapDefendersCount")

    val notDefendersCount = parseResultDirty.players.count { filter.PlayerFilter.Not(defenderFilter).matches(it) }
    println("Игроков не-защитников: $notDefendersCount")

    println()
    println("п.12. Мини-DSL отчёта:")

    val reportText = report {
        metric("Без агентства") { parseResultDirty.players.count { it.agency == null } }
        section("Атаки") {
            metric("Всего голов") { parseResultDirty.players.sumOf { it.goals } }
            metric("Всего голевых передач") { parseResultDirty.players.sumOf { it.assists } }
        }
        section("Дисциплина") {
            metric("Всего жёлтых карточек") { parseResultDirty.players.sumOf { it.yellowCards } }
            metric("Всего красных карточек") { parseResultDirty.players.sumOf { it.redCards } }
        }
    }
    println(reportText)

    println()
    println("ЗАДАНИЕ 4. Вариант 1. Состав (JFreeChart)")

    println("Базовая: доля игроков по позициям")
    val distribution = PositionDistribution(parseResultDirty.players)
    distribution.counts.forEach { (name, count) ->
        val share = distribution.shareOf(name) * 100
        println("  $name: $count игроков (%.1f%%)".format(share))
    }
    PositionShareChart(distribution).render("position_share.png")
    println("  График сохранён: position_share.png")

    println()
    println("Продвинутая: структура топ-5 команд по составу")
    val composition = TopTeamsComposition(parseResultDirty.players, topN = 5)
    composition.teams.forEach { team ->
        println("  ${team.name}: стоимость состава = ${team.totalTransferCost}")
    }
    TeamCompositionChart(composition).render("top_teams_composition.png")
    println("  График сохранён: top_teams_composition.png")
}