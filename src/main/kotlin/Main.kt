import filter.And
import filter.ByPosition
import filter.CheaperThan
import filter.Not
import java.nio.file.Path
import model.Position
import model.Team
import parser.CsvParser
import report.report
import resolver.Resolver
import visualization.ChartVisualizer

private fun loadResource(name: String) =
    object {}.javaClass.classLoader.getResourceAsStream(name) ?: error("Файл $name не найден")

fun main() {
    val fileName = "fakePlayersDirty.csv"
    val result = CsvParser.parse(loadResource(fileName))
    val players = result.players
    val resolver = Resolver(players)

    println("=== Задание 1. Модель и маппинг ===")
    println("Файл: $fileName")
    println("Распознано игроков: ${players.size}")
    println("Пропущено строк: ${result.skippedCount}")

    println("\n=== Задание 2. Запросы ===")
    println("1. Количество игроков без агенства: ${resolver.getCountWithoutAgency()}")

    val (defenderName, defenderGoals) = resolver.getBestScorerDefender()
    println("2. Автор наибольшего числа голов среди защитников: $defenderName ($defenderGoals)")
    println("3. Позиция самого дорогого немецкого игрока: ${resolver.getTheExpensiveGermanPlayerPosition()}")
    println("4. Команда с наибольшим средним числом красных карточек на одного игрока: ${resolver.getTheRudestTeam().name}")

    println("5. Средняя трансферная стоимость по позициям:")
    resolver.getAverageTransferCostByPosition().forEach { (position, averageCost) ->
        println("   ${position.russianName}: ${"%.3f".format(averageCost)}")
    }

    println("6. Топ-3 полезных игроков:")
    resolver.getMostValuablePlayers().forEachIndexed { index, player ->
        val value = player.goals + 2 * player.assists
        println("   ${index + 1}. ${player.name} - $value")
    }

    println("7. Самое популярное агентство по странам:")
    resolver.getMostPopularAgencyByCountry().toSortedMap().forEach { (country, agency) ->
        println("   $country: $agency")
    }

    val (belowMedianShare, aboveMedianShare) = resolver.getGoalsShareByMedianCost()
    println("8. Доля голов при стоимости не выше медианы: ${"%.4f".format(belowMedianShare)}")
    println("   Доля голов при стоимости выше медианы: ${"%.4f".format(aboveMedianShare)}")

    println("\n=== Задание 4. Визуализация (Вариант 3) ===")
    val charts = ChartVisualizer.createCharts(players, Path.of("build", "charts"))
    println("Гистограмма: ${charts.costDistribution}")
    println("Сравнение позиций: ${charts.costByPosition}")

    println("\n=== Задание 3. Запросы (дополнительные) ===")

    println("\n9. Команда-агрегат (ленивые Team.totalTransferCost и Team.averageRedCards):")
    val teams = players.map { it.team }.distinct()
    println("   Всего команд: ${teams.size}")
    teams.sortedWith(compareByDescending<Team> { it.totalTransferCost }).forEach { team ->
        println("   ${team.name}: стоимость состава = ${team.totalTransferCost}, средние красные карточки = ${"%.2f".format(team.averageRedCards)}")
    }

    val streamedPlayers = loadResource(fileName).use(CsvParser::findHighValueDisciplinedPlayers)
    println("\n10. Потоковое чтение:")
    println("    Первый игрок стоимостью больше 50 000 000, проведший не менее 25 матчей и ни разу не получавший КРАСНУЮ карточку: ${streamedPlayers.playerWithoutRedCards?.name ?: "не найден"}")
    println("    Первый игрок стоимостью больше 50 000 000, проведший не менее 25 матчей и ни разу не получавший ЖЕЛТУЮ карточку: ${streamedPlayers.playerWithoutYellowCards?.name ?: "не найден"}")
    println("    Первый игрок стоимостью больше 50 000 000, проведший не менее 25 матчей и ни разу не получавший НИ ОДНУ карточку: ${streamedPlayers.playerWithoutAnyCards?.name ?: "не найден"}")

    println("\n11. Фильтры:")
    val defenders = ByPosition(Position.DEFENDER)
    val bestDefenderViaFilter = players
        .filter { defenders.matches(it) }
        .sortedBy { it.name }
        .maxByOrNull { it.goals }
    println("    Переформулировка запроса из задания 2 (пример):")
    println("        2'. Автор наибольшего числа голов среди защитников через ByPosition: ${bestDefenderViaFilter?.name} (${bestDefenderViaFilter?.goals})")

    val expensiveDefenders = And(ByPosition(Position.DEFENDER), Not(CheaperThan(50_000_000)))
    val expensiveDefendersCount = players.count { expensiveDefenders.matches(it) }
    println("    Дорогие защитники (стоимость > 50 000 000) через And/Not/CheaperThan: $expensiveDefendersCount")

    println("\n12. Мини-DSL отчёта:")
    report {
        metric("Без агентства") { players.count { it.agency == null } }
        metric("Всего игроков") { players.size }
        section("Атаки") {
            metric("Всего голов") { players.sumOf { it.goals } }
            metric("Всего голевых передач") { players.sumOf { it.assists } }
        }
        section("Дисциплина") {
            metric("Жёлтые карточки") { players.sumOf { it.yellowCards } }
            metric("Красные карточки") { players.sumOf { it.redCards } }
        }
    }
}