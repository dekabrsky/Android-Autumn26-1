import parser.CsvParser
import resolver.Resolver
import java.util.Locale

import visualization.Variant2Charts

fun main() {
    val inputStream = object {}.javaClass
        .getResourceAsStream("/fakePlayersDirty.csv")

    if (inputStream == null) {
        println("Файл не найден")
        return
    }

    val parseResult = CsvParser.parse(inputStream)
    val resolver = Resolver(parseResult.players)

    println("=== Задание 1. Парсинг CSV ===")
    println("Успешно прочитано игроков: ${parseResult.players.size}")
    println("Пропущено строк: ${parseResult.skippedCount}")

    println()
    println("=== Задание 2. Запросы ===")

    println("1. Игроков без агентства:")
    println(resolver.getCountWithoutAgency())

    println("\n2. Лучший бомбардир среди защитников:")
    val bestDefender = resolver.getBestScorerDefender()
    println("${bestDefender.first} — ${bestDefender.second} голов")

    println("\n3. Позиция самого дорогого немецкого игрока:")
    println(resolver.getTheExpensiveGermanPlayerPosition())

    println("\n4. Самая грубая команда:")
    val rudestTeam = resolver.getTheRudestTeam()
    println("${rudestTeam.name} (${rudestTeam.city})")

    println("\n5. Средняя стоимость по позициям:")
    resolver.getAverageTransferCostByPosition()
        .forEach { (position, averageCost) ->
            println(
                "${position.russianName}: ${
                    String.format(Locale.US, "%,.2f", averageCost)
                }"
            )
        }

    println("\n6. Топ-3 самых полезных игроков:")
    resolver.getMostValuablePlayers()
        .forEach { player ->
            val score = player.goals + 2 * player.assists
            println("${player.name}: $score баллов")
        }

    println("\n7. Самое популярное агентство по странам:")
    resolver.getMostPopularAgencyByCountry()
        .forEach { (country, agency) ->
            println("$country -> $agency")
        }

    println("\n8. Доли голов относительно медианы:")
    val shares = resolver.getGoalsShareByMedianCost()

    println(
        "Не дороже медианы: ${
            String.format(Locale.US, "%.2f", shares.first * 100)
        }%"
    )

    println(
        "Дороже медианы: ${
            String.format(Locale.US, "%.2f", shares.second * 100)
        }%"
    )

    println()
    println("=== Задание 4. Вариант 2 ===")
    println("Открываются графики...")

    Variant2Charts.show(parseResult.players)
}