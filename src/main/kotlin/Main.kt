import parser.CsvParser
import resolver.Resolver
import java.io.File

fun main() {
    val input = object {}.javaClass.classLoader.getResourceAsStream("fakePlayersDirty.csv")
        ?: error("Файл fakePlayersDirty.csv не найден")

    val result = CsvParser.parse(input)
    val players = result.players
    val resolver = Resolver(players)

    println("Игроков успешно загружено: ${players.size}")
    println("Пропущено строк: ${result.skippedCount}")
    println()

    println("1. Игроков без агентства: ${resolver.getCountWithoutAgency()}")
    println()

    val bestDefender = resolver.getBestScorerDefender()
    println("2. Лучший бомбардир среди защитников:")
    println("${bestDefender.first}: ${bestDefender.second}")
    println()

    println("3. Позиция самого дорогого немецкого игрока:")
    println(resolver.getTheExpensiveGermanPlayerPosition())
    println()

    val rudestTeam = resolver.getTheRudestTeam()
    println("4. Команда с максимальным средним числом красных карточек:")
    println("${rudestTeam.name}, ${rudestTeam.city}")
    println()

    println("5. Средняя трансферная стоимость по позициям:")
    resolver.getAverageTransferCostByPosition().forEach { (position, cost) ->
        println("${position.russianName}: ${"%,.2f".format(cost)}")
    }
    println()

    println("6. Топ-3 полезных игроков:")
    resolver.getMostValuablePlayers().forEachIndexed { index, player ->
        val score = player.goals + 2 * player.assists
        println("${index + 1}. ${player.name}: $score")
    }
    println()

    println("7. Самое популярное агентство по странам:")
    resolver.getMostPopularAgencyByCountry().toSortedMap().forEach { (country, agency) ->
        println("$country: $agency")
    }
    println()

    val goalShares = resolver.getGoalsShareByMedianCost()
    println("8. Доля голов относительно медианной стоимости:")
    println("Не дороже медианы: ${"%.2f".format(goalShares.first * 100)}%")
    println("Дороже медианы: ${"%.2f".format(goalShares.second * 100)}%")
    println()

    val chartsDirectory = File("charts")
    chartsDirectory.mkdirs()

    Charts.saveTransferCostHistogram(players, File(chartsDirectory, "variant3_distribution.png"))
    Charts.saveTransferCostByPosition(players, File(chartsDirectory, "variant3_positions.png"))

    println("Графики варианта 3 сохранены в папку charts")
}
