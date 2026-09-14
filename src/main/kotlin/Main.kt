import parser.CsvParser
import resolver.Resolver
import visualization.TransferCostCharts

fun main() {
    val inputStream = requireNotNull(ClassLoader.getSystemResourceAsStream("fakePlayersDirty.csv")) {
        "Resource fakePlayersDirty.csv was not found"
    }
    val parseResult = CsvParser.parse(inputStream)
    val players = parseResult.players
    val resolver = Resolver(players)

    println("Футболисты загружены: ${players.size}")
    println("Строк пропущено: ${parseResult.skippedCount}")
    println()

    val bestDefender = resolver.getBestScorerDefender()
    val goalsShare = resolver.getGoalsShareByMedianCost()

    println("1. Игроков без агентства: ${resolver.getCountWithoutAgency()}")
    println("2. Лучший бомбардир среди защитников: ${bestDefender.first}, голов: ${bestDefender.second}")
    println("3. Позиция самого дорогого немецкого игрока: ${resolver.getTheExpensiveGermanPlayerPosition()}")
    println("4. Самая грубая команда: ${resolver.getTheRudestTeam()}")
    println("5. Средняя трансферная стоимость по позициям:")
    resolver.getAverageTransferCostByPosition().forEach { (position, averageCost) ->
        println("   ${position.russianName}: %.2f".format(averageCost))
    }
    println("6. Топ-3 полезных игроков:")
    resolver.getMostValuablePlayers().forEachIndexed { index, player ->
        println("   ${index + 1}. ${player.name}: ${player.goals + 2 * player.assists}")
    }
    println("7. Самое популярное агентство по странам:")
    resolver.getMostPopularAgencyByCountry().forEach { (country, agency) ->
        println("   $country: $agency")
    }
    println(
        "8. Доля голов игроков не дороже медианы: %.4f, дороже медианы: %.4f"
            .format(goalsShare.first, goalsShare.second),
    )
    println()

    println("Графики:")
    TransferCostCharts.generateAll(players).forEach { file ->
        println(file.absolutePath)
    }
}
