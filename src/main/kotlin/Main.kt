import filter.PlayerFilter
import filter.matches
import model.Position
import model.Team
import parser.CsvParser
import report.report
import resolver.Resolver
import streaming.CsvStreaming
import vizualization.Charts

fun main() {
    val parseResult = CsvParser.parseResource("fakePlayersDirty.csv")
    val players = parseResult.players

    println("Задание 1: Модель и маппинг")
    println("Загружено игроков: ${players.size}" +
            "\nПропущено строк: ${parseResult.skippedCount}")

    val resolver = Resolver(players)

    println()

    println("Задание 2: обязательные запросы")
    println("1. Игроков без агентства: ${resolver.getCountWithoutAgency()}")

    val (scorerName, scorerGoals) = resolver.getBestScorerDefender()
    println("2. Лучший бомбардир-защитник: $scorerName, голов: $scorerGoals")
    println("3. Позиция самого дорогого немецкого игрока: ${resolver.getTheExpensiveGermanPlayerPosition()}")

    val rudestTeam: Team = resolver.getTheRudestTeam()
    println("4. Самая грубая команда: ${rudestTeam.name}")

    println("5. Средняя трансферная стоимость по позициям (по убыванию):")
    resolver.getAverageTransferCostByPosition().forEach { (position, average) ->
        println("   ${position.russianName}: %.2f".format(average))
    }

    println("6. Топ-3 самых полезных игрока (голы + 2 × передачи):")
    resolver.getMostValuablePlayers().forEachIndexed { index, player ->
        println("   ${index + 1}. ${player.name} — ${player.impact}")
    }

    println("7. Самое популярное агентство по странам (первые 5 стран):")
    resolver.getMostPopularAgencyByCountry().entries.take(5).forEach { (country, agency) ->
        println("   $country -> $agency")
    }

    val (shareNotAboveMedian, shareAboveMedian) = resolver.getGoalsShareByMedianCost()
    println(
        "8. Доля голов игроков не дороже медианы: %.2f%%, дороже медианы: %.2f%%"
            .format(shareNotAboveMedian * 100, shareAboveMedian * 100)
    )

    println()

    println("Задание 3: дополнительные задания")

    println(
        "9. У самой грубой команды (${rudestTeam.name}) суммарная стоимость состава: " +
                "${rudestTeam.totalTransferCost}, среднее число красных карточек на игрока: " +
                "%.2f".format(rudestTeam.averageRedCards)
    )

    val streamingCsvPath = "src/main/resources/fakePlayersDirty.csv"
    val streamedPlayer = CsvStreaming.findFirstExpensiveCleanPlayer(streamingCsvPath)
    println(
        "10. Первый дорогой игрок (>50 000 000, ≥25 матчей, без карточек), найденный потоково: " +
                (streamedPlayer?.name ?: "такой игрок не найден")
    )

    val defenderFilter = PlayerFilter.ByPosition(Position.DEFENDER)
    val bestDefenderViaFilter = players.filter { defenderFilter.matches(it) }.maxByOrNull { it.goals }
    println(
        "11. Лучший бомбардир-защитник через фильтр ByPosition: " +
                "${bestDefenderViaFilter?.name}, голов: ${bestDefenderViaFilter?.goals}"
    )
    val cheapDefendersFilter = PlayerFilter.And(defenderFilter, PlayerFilter.CheaperThan(20_000_000))
    println("    Защитников дешевле 20 000 000 (And): ${players.count { cheapDefendersFilter.matches(it) }}")
    val notDefendersFilter = PlayerFilter.Not(defenderFilter)
    println("    Игроков не-защитников (Not): ${players.count { notDefendersFilter.matches(it) }}")

    println("12. DSL-отчёт")
    println(
        report {
            metric("Без агентства") { players.count { it.agency == null } }
            section("Атаки") {
                metric("Всего голов") { players.sumOf { it.goals } }
                metric("Всего голевых передач") { players.sumOf { it.assists } }
            }
            section("Дисциплина") {
                metric("Жёлтые карточки") { players.sumOf { it.yellowCards } }
                metric("Красные карточки") { players.sumOf { it.redCards } }
            }
        }
    )

    val positionChart = Charts.positionShareChart(players)
    val compositionChart = Charts.topTeamsPositionCompositionChart(resolver.teams)

    Charts.save(positionChart, "charts/position_share.png")
    Charts.save(compositionChart, "charts/top_teams_composition.png")

    Charts.display(positionChart, "Доля игроков по позициям")
    Charts.display(compositionChart, "Состав топ-команд")
}
