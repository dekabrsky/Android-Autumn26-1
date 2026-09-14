import parser.CsvParser
import resolver.Resolver
import visualization.Variant3Charts

fun main() {
    val inputStream = checkNotNull(
        object {}.javaClass.classLoader
            .getResourceAsStream("fakePlayersDirty.csv")
    ) {
        "Ресурс fakePlayersDirty.csv не найден"
    }

    val parseResult = CsvParser.parse(inputStream)
    val players = parseResult.players
    val resolver = Resolver(players)

    println("Загружено игроков: ${players.size}")
    println("Пропущено строк: ${parseResult.skippedCount}")
    println()

    println("1. Без агентства: ${resolver.getCountWithoutAgency()}")

    val bestDefender = resolver.getBestScorerDefender()
    println(
        "2. Лучший защитник: ${bestDefender.first}, " +
                "голов: ${bestDefender.second}"
    )

    println(
        "3. Позиция самого дорогого немецкого игрока: " +
                resolver.getTheExpensiveGermanPlayerPosition()
    )

    val rudestTeam = resolver.getTheRudestTeam()
    println(
        "4. Самая грубая команда: " +
                "${rudestTeam.name} (${rudestTeam.city})"
    )

    println("5. Средняя стоимость по позициям:")
    resolver.getAverageTransferCostByPosition()
        .forEach { (position, average) ->
            println(
                "   ${position.russianName}: " +
                        "%.2f".format(average)
            )
        }

    println("6. Топ-3 полезных игроков:")
    resolver.getMostValuablePlayers().forEachIndexed { index, player ->
        val score = player.goals + 2 * player.assists
        println("   ${index + 1}. ${player.name}: $score")
    }

    println("7. Популярные агентства по странам:")
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

    val chartFiles = Variant3Charts.save(players)

    println()
    println("Графики варианта 3 сохранены:")
    chartFiles.forEach {
        println("   ${it.absolutePath}")
    }
}