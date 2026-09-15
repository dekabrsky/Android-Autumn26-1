import parser.CsvParser
import resolver.Resolver

fun main() {
    val inputStream = object {}.javaClass.classLoader
        .getResourceAsStream("fakePlayersDirty.csv")
        ?: error("Ресурс fakePlayersDirty.csv не найден")

    val parseResult = CsvParser.parse(inputStream)
    val resolver = Resolver(parseResult.players)

    println("Загружено игроков: ${parseResult.players.size}")
    println("Пропущено строк: ${parseResult.skippedCount}")
    println()

    println(
        "1. Количество игроков без агентства: " +
                resolver.getCountWithoutAgency()
    )

    val bestDefender = resolver.getBestScorerDefender()
    println(
        "2. Лучший бомбардир среди защитников: " +
                "${bestDefender.first}, голов: ${bestDefender.second}"
    )

    println(
        "3. Позиция самого дорогого немецкого игрока: " +
                resolver.getTheExpensiveGermanPlayerPosition()
    )

    val rudestTeam = resolver.getTheRudestTeam()
    println(
        "4. Команда с наибольшим средним числом красных карточек: " +
                "${rudestTeam.name} (${rudestTeam.city})"
    )

    println("5. Средняя трансферная стоимость по позициям:")
    resolver.getAverageTransferCostByPosition()
        .forEach { (position, averageCost) ->
            println(
                "   ${position.russianName}: " +
                        "%,.2f".format(averageCost)
            )
        }

    println("6. Топ-3 самых полезных игроков:")
    resolver.getMostValuablePlayers()
        .forEachIndexed { index, player ->
            val usefulness = player.goals + 2 * player.assists

            println(
                "   ${index + 1}. ${player.name}: $usefulness"
            )
        }

    println("7. Самые популярные агентства по странам:")
    resolver.getMostPopularAgencyByCountry()
        .forEach { (country, agency) ->
            println("   $country: $agency")
        }

    val goalsShares = resolver.getGoalsShareByMedianCost()

    println(
        "8. Доля голов игроков не дороже медианы: " +
                "%.2f%%".format(goalsShares.first * 100)
    )

    println(
        "   Доля голов игроков дороже медианы: " +
                "%.2f%%".format(goalsShares.second * 100)
    )
}