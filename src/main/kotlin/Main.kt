import parser.CsvParser
import resolver.Resolver
import visualization.createVariant2Charts

fun main() {
    val input = Thread.currentThread()
        .contextClassLoader
        .getResourceAsStream("fakePlayersDirty.csv")
        ?: error("Не найден fakePlayersDirty.csv")

    val result = CsvParser.parse(input)

    println("Загружено игроков: ${result.players.size}")
    println("Пропущено строк: ${result.skippedCount}")
    println()

    val resolver = Resolver(result.players)

    println("===== ЗАДАНИЕ 2 =====")

    println()
    println("1. Игроков без агентства:")
    println(resolver.getCountWithoutAgency())

    println()
    println("2. Лучший бомбардир среди защитников:")
    println(resolver.getBestScorerDefender())

    println()
    println("3. Позиция самого дорогого немецкого игрока:")
    println(resolver.getTheExpensiveGermanPlayerPosition())

    println()
    println("4. Команда с наибольшим средним числом красных карточек:")
    println(resolver.getTheRudestTeam())

    println()
    println("5. Средняя трансферная стоимость по позициям:")
    resolver.getAverageTransferCostByPosition()
        .forEach { (position, average) ->
            println("${position.russianName}: $average")
        }

    println()
    println("6. Топ-3 самых полезных игроков:")
    resolver.getMostValuablePlayers()
        .forEach { player ->
            val score = player.goals + 2 * player.assists
            println("${player.name}: $score")
        }

    println()
    println("7. Самое популярное агентство по странам:")
    resolver.getMostPopularAgencyByCountry()
        .forEach { (country, agency) ->
            println("$country: $agency")
        }

    println()
    println("8. Доли голов относительно медианной стоимости:")
    val shares = resolver.getGoalsShareByMedianCost()

    println("Не дороже медианы: ${shares.first}")
    println("Дороже медианы: ${shares.second}")

    println()
    println("===== ЗАДАНИЕ 4 =====")

    createVariant2Charts(result.players)
}