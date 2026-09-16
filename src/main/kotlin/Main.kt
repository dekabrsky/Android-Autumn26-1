import parser.CsvParser
import resolver.Resolver
import java.util.Locale
import visualization.FootballCharts


fun main() {
    val stream = object {}.javaClass
        .getResourceAsStream("/fakePlayersDirty.csv")

    if (stream == null) {
        println("Не удалось открыть CSV-файл")
        return
    }

    val parseResult = CsvParser.parse(stream)
    val resolver = Resolver(parseResult.players)

    println("Задание 1. Обработка CSV-файла")
    println("Корректно обработано игроков: ${parseResult.players.size}")
    println("Пропущено некорректных строк: ${parseResult.skippedCount}")

    println()
    println("Задание 2. Запросы к данным")

    println("1) Количество игроков без агентства: ${resolver.getCountWithoutAgency()}")

    val bestDefender = resolver.getBestScorerDefender()
    println(
        "2) Лучший бомбардир среди защитников: " +
                "${bestDefender.first}, голов: ${bestDefender.second}"
    )

    println(
        "3) Позиция самого дорогого игрока из Германии: " +
                resolver.getTheExpensiveGermanPlayerPosition()
    )

    val rudestTeam = resolver.getTheRudestTeam()
    println(
        "4) Команда с максимальным средним числом красных карточек: " +
                "${rudestTeam.name} (${rudestTeam.city})"
    )

    println("5) Средняя трансферная стоимость по позициям:")
    resolver.getAverageTransferCostByPosition()
        .forEach { (position, averageCost) ->
            println(
                "   ${position.russianName}: ${
                    String.format(Locale.US, "%,.2f", averageCost)
                }"
            )
        }

    println("6) Три самых полезных игрока:")
    resolver.getMostValuablePlayers()
        .forEachIndexed { index, player ->
            val points = player.goals + 2 * player.assists
            println("   ${index + 1}. ${player.name} — $points баллов")
        }

    println("7) Самое популярное агентство по каждой стране:")
    resolver.getMostPopularAgencyByCountry()
        .forEach { (country, agency) ->
            println("   $country: $agency")
        }

    val shares = resolver.getGoalsShareByMedianCost()

    println("8) Распределение голов относительно медианной стоимости:")
    println(
        "   Игроки не дороже медианы: ${
            String.format(Locale.US, "%.2f", shares.first * 100)
        }%"
    )
    println(
        "   Игроки дороже медианы: ${
            String.format(Locale.US, "%.2f", shares.second * 100)
        }%"
    )
    println()
    println("Задание 4. Визуализация данных")
    println("Открываются графики...")

    FootballCharts.showCharts(parseResult.players)
}