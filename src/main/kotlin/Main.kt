import filter.PlayerFilter
import model.Position
import model.Team
import parser.CsvParser
import report.report
import resolver.Resolver
import streaming.CsvStreaming
import visualization.PlayerVisualization

fun main() {
    val datasetResource = "fakePlayersDirty.csv"
    val parsingResult = CsvParser.parseResource(datasetResource)
    val playerList = parsingResult.players

    println("Успешно обработано записей: ${playerList.size}")
    println("Игнорировано невалидных строк: ${parsingResult.skippedCount}\n")

    val analyticsResolver = Resolver(playerList)

    println("1. Количество атлетов без контракта с агентством: ${analyticsResolver.getCountWithoutAgency()}")

    val (topDefender, defenderGoalCount) = analyticsResolver.getBestScorerDefender()
    println("2. Наиболее результативный защитник: $topDefender (забито мячей: $defenderGoalCount)")

    val expensiveGermanRole = analyticsResolver.getTheExpensiveGermanPlayerPosition()
    println("3. Позиция самого высокооплачиваемого игрока из Германии: $expensiveGermanRole")

    val aggressiveClub: Team = analyticsResolver.getTheRudestTeam()
    println("4. Самый грубый клуб: ${aggressiveClub.name}")

    println("5. Средний ценник трансфера по ролям (по убыванию):")
    analyticsResolver.getAverageTransferCostByPosition().forEach { (role, avgCost) ->
        println("   ${role.russianName}: ${"%.2f".format(avgCost)}")
    }

    println("6. Топ 3 полезных игрока:")
    analyticsResolver.getMostValuablePlayers().take(3).forEachIndexed { rank, player ->
        println("   ${rank + 1}. ${player.name} — ${player.impact}")
    }

    println("7. Главные агентства по странам (топ-5):")
    analyticsResolver.getMostPopularAgencyByCountry().entries.take(5).forEach { (nation, mainAgency) ->
        println("   $nation -> $mainAgency")
    }

    val (underOrAtMedianPercent, aboveMedianPercent) = analyticsResolver.getGoalsShareByMedianCost()
    println(
        "8. Распределение голов — до медианной стоимости включительно: ${"%.2f%%".format(underOrAtMedianPercent * 100)}, " +
                "свыше медианы: ${"%.2f%%".format(aboveMedianPercent * 100)}\n"
    )

    println(
        "9. Показатели клуба ${aggressiveClub.name}: суммарная стоимость ростера = " +
                "${aggressiveClub.totalTransferCost}, средняя норма красных карточек на человека = " +
                "%.2f".format(aggressiveClub.averageRedCards)
    )

    val streamingSourceFile = "src/main/resources/fakePlayersDirty.csv"
    val streamedMatch = CsvStreaming.findFirstExpensiveCleanPlayer(streamingSourceFile)
    val matchName = streamedMatch?.name ?: "совпадений не найдено"
    println("10. Первый дорогой игрок (ценник >50M, от 25 игр, 0 карточек), найденный в потоке: $matchName")

    val isDefender = PlayerFilter.ByPosition(Position.DEFENDER)
    val maxScoringDefender = playerList.filter(isDefender::matches).maxByOrNull { it.goals }
    println("11. Топ-бомбардир среди защитников (через ByPosition): ${maxScoringDefender?.name}, голов: ${maxScoringDefender?.goals ?: 0}")

    val affordableDefenders = isDefender and PlayerFilter.CheaperThan(40_000_000)
    val affordableDefendersCount = playerList.count { affordableDefenders.matches(it) }
    println("    Защитников с ценой менее 40 000 000 (And): $affordableDefendersCount")

    val nonForwarder = !PlayerFilter.ByPosition(Position.FORWARD)
    val nonForwarderCount = playerList.count { nonForwarder.matches(it) }
    println("    Все позиции кроме форвардов (Not): $nonForwarderCount")

    println("12. DSL-отчёт")
    val generatedReport = report {
        metric("Без агентства") { playerList.count { it.agency == null } }
        section("Атаки") {
            metric("Всего голов") { playerList.sumOf { it.goals } }
            metric("Всего голевых передач") { playerList.sumOf { it.assists } }
        }
        section("Дисциплина") {
            metric("Жёлтые карточки") { playerList.sumOf { it.yellowCards } }
            metric("Красные карточки") { playerList.sumOf { it.redCards } }
        }
    }
    println(generatedReport)

    val costChart = PlayerVisualization.transferCostChart(playerList)
    val dispersionChart = PlayerVisualization.transferDispersionChart(analyticsResolver.teams)

    val costChartPath = "charts/transferCost.png"
    val dispersionChartPath = "charts/transferDispersion.png"

    PlayerVisualization.save(costChart, costChartPath)
    PlayerVisualization.save(dispersionChart, dispersionChartPath)

    PlayerVisualization.display(costChart, "Трансферные стоимости")
    PlayerVisualization.display(dispersionChart, "Разброс трансферных стоимостей")
}