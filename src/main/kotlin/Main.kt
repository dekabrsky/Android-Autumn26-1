import model.Team
import parser.CsvParser
import resolver.Resolver
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt

fun main(args: Array<String>) {
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
            println("${index + 1}. ${team.name} (${team.city}) - ${team.numberOfPlayers} игроков ")
        }

    println()

    val resolver = Resolver(result.players)

    println("========================= 2 ЗАДАНИЕ =========================")
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
        println("   ${position.russianName}: ${String.format(Locale("ru"), "%,.2f", cost)}")
    }

    println("\n6. Топ 3 самых полезных игроков:")
    resolver.getMostValuablePlayers().forEachIndexed { index, player ->
        println("   ${index + 1}. ${player.name} | ${player.goals} голов | ${player.assists} ассистов || Баллы: ${player.goals + player.assists * 2}")
    }

    println("\n7. Топ 3 самых популярных агентств по странам:")
    resolver.getMostPopularAgencyByCountry().forEach { (country, agency) ->
        println("$country | $agency")
    }

    println("\n8. Доля забитых голов игроками ниже медина стоимости и выше:")
    resolver.getGoalsShareByMedianCost().let { (less, more) ->
        println("Ниже - ${"%.3f".format(less)} или ${(less * 100).roundToInt()}% | Выше - ${"%.3f".format(more)} или ${(more * 100).roundToInt()}%")
    }
}