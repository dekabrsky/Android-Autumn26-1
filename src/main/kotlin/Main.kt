import charts.buildPositionChart
import charts.buildTop5TeamsChart
import dsl.ReportBuilder
import model.Team
import parser.CsvParser
import resolver.Resolver
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import dsl.report

fun main() {
    println("Yeah rock!\n")

    val stream = CsvParser::class.java.getResourceAsStream("/fakePlayersDirty.csv") ?: error("Файл не найден")

    val result = CsvParser.parse(stream)

    println("========================= 1 ЗАДАНИЕ =========================")
    println("Игроков загружено: ${result.players.size}, пропущено: ${result.skippedCount}\n")

    println("========================= ДЛЯ СПРАВОЧКИ =========================")
    println("Список команд:")
    result.players
        .distinctBy { it.team }
        .map { it.team }
        .forEachIndexed { index, team ->
            println("   ${index + 1}. ${team.name} (${team.city}) - ${team.numberOfPlayers} игроков ")
        }

    println()

    println("========================= 2 ЗАДАНИЕ =========================")
    val resolver = Resolver(result.players)

    println("1. Игроков без агентства: ${resolver.getCountWithoutAgency()}\n")

    resolver.getBestScorerDefender().let { (name, goals) ->
        println("2. Лучший бомбардир-защитник: $name - $goals голов\n")
    }

    println("3. Позиция самого дорого немца: ${resolver.getTheExpensiveGermanPlayerPosition()}\n")

    resolver.getTheRudestTeam().let { (name, city, quantity) ->
        println("4. Команда с наибольшим средним числом красных карточек на одного игрока:\n" +
                "   Команда - $name\n" +
                "   Город - $city\n" +
                "   Количество игроков в команде - $quantity\n")
    }

    println("5. Средняя стоимость по позициям:")
    resolver.getAverageTransferCostByPosition().forEach { (position, cost) ->
        // Удобный вывод длинных чисел
        println("   ${position.russianName}: ${String.format(Locale("ru"), "%,.2f", cost)} $")
    }

    println("\n6. Топ 3 самых полезных игроков:")
    resolver.getMostValuablePlayers().forEachIndexed { index, player ->
        println("   ${index + 1}) ${player.name} | ${player.goals} голов | ${player.assists} ассистов || Баллы: ${player.goals + player.assists * 2}")
    }

    println("\n7. Топ 3 самых популярных агентств по странам:")
    resolver.getMostPopularAgencyByCountry().forEach { (country, agency) ->
        println("   $country | $agency")
    }

    println("\n8. Доля забитых голов игроками ниже медины стоимости и выше:")
    resolver.getGoalsShareByMedianCost().let { (less, more) ->
        println("Ниже - ${"%.3f".format(less)} или ${(less * 100).roundToInt()}% | Выше - ${"%.3f".format(more)} или ${(more * 100).roundToInt()}%")
    }

    println("\n========================= 3 ЗАДАНИЕ =========================")
    val testTeam = result.players.groupBy { it.team }

    println("9. Команда-агрегат. Список всех игроков по командам, общая стоимость клуба и среднее количество карточек")
    testTeam.entries.forEachIndexed { index, entry ->
        val team = entry.key
        val playersOfTeam = entry.value
        team.players.addAll(playersOfTeam)

        val playersInfo = team.players.joinToString("  ||  ") { player ->
            val formattedCost = String.format(Locale("ru"), "%,d", player.transferCost)
            "${player.name} — $formattedCost $"
        }

        println(" ${index + 1}) Команда - ${team.name} (${team.city}) | Количество игроков в команде - ${team.numberOfPlayers}\n" +
                "    Общая трансферная стоимость клуба: ${String.format(Locale("ru"), "%,d", team.totalTransferCost)}\n" +
                "    Среднее количество карточек: ${"%.1f".format(team.averageCards)}\n" +
                "    Игроки находящиеся в команде:\n    $playersInfo\n")
    }

    println("10. Первый игрок со стоимостью больше 50 млн, отыгравший более 25 матчей и не получивший ни одной карточки:")
    val searchStream = CsvParser::class.java.getResourceAsStream("/fakePlayersDirty.csv") ?: error("Файл не найден")
    val superPlayer = CsvParser.findFirstMatchingPlayer(searchStream)
    if (superPlayer == null) {
        println("Такого игрока нет(\n")
    } else {
        println(superPlayer)
    }

    println("11. Фильтры (задания 2.2 и 2.8:")

    resolver.getBestScorerDefenderWithFilter().let { (name, goals) ->
        println("   2.2. Лучший бомбардир-защитник (с фильтрами): $name - $goals голов (с фильтрами)\n")
    }

    println("   2.8. Доля забитых голов игроками ниже медины стоимости и выше (с фильтрами):")
    resolver.getGoalsShareByMedianCostWithFilter().let { (less, more) ->
        println("   Ниже - ${"%.3f".format(less)} или ${(less * 100).roundToInt()}% | Выше - ${"%.3f".format(more)} или ${(more * 100).roundToInt()}%\n")
    }

    println("12. Мини-DSL отчёта")

    report {
        metric("Без агентств") { result.players.count { it.agency == null } }
        section("Атаки") {
            metric("Всего голов") { result.players.sumOf { it.goals }}
            metric("Всего ассистов") {result.players.sumOf { it.assists }}
        }
        section("Цены игроков") {
            metric("Стоимость самого дорогого игрока") {
                String.format(Locale("ru"), "%,d",result.players.maxOf { it.transferCost })
            }
            metric("Стоимость самого дешевого игрока") {
                String.format(Locale("ru"), "%,d",result.players.minOf { it.transferCost })
            }
        }
    }

    println("\n========================= 4 ЗАДАНИЕ =========================")
    println("График первого задания открылся сам, второго - создались в папке lets-plot-images")
    buildPositionChart(result.players)
    buildTop5TeamsChart(result.players)
}