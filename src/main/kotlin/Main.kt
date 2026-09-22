import charts.Variant3Charts
import parser.CsvParser
import resolver.Resolver
import java.io.File

fun main() {
    val result = CsvParser.parseResource("fakePlayersDirty.csv")
    val players = result.players
    val resolver = Resolver(players)

    println("Загружено игроков: ${players.size}")
    println("Пропущено строк: ${result.skippedCount}")
    println()

    println("1. Без агентства: ${resolver.getCountWithoutAgency()}")

    val defender = resolver.getBestScorerDefender()
    println("2. Лучший защитник: ${defender.first}, голов: ${defender.second}")

    println("3. Позиция самого дорогого немца: ${resolver.getTheExpensiveGermanPlayerPosition()}")

    val rudest = resolver.getTheRudestTeam()
    println("4. Самая высокая средняя доля красных карточек: ${rudest.name}, ${rudest.city}")

    println("5. Средняя стоимость по позициям:")
    resolver.getAverageTransferCostByPosition().forEach { (position, average) ->
        println("   ${position.russianName}: %.2f".format(average))
    }

    println("6. Топ-3 самых полезных игроков:")
    resolver.getMostValuablePlayers().forEach { player ->
        val usefulness = player.goals + 2 * player.assists
        println("   ${player.name}: $usefulness")
    }

    println("7. Самое популярное агентство по странам:")
    resolver.getMostPopularAgencyByCountry().forEach { (country, agency) ->
        println("   $country: $agency")
    }

    val shares = resolver.getGoalsShareByMedianCost()
    println("8. Доля голов:")
    println("   не дороже медианы: %.4f (%.2f%%)".format(shares.first, shares.first * 100))
    println("   дороже медианы: %.4f (%.2f%%)".format(shares.second, shares.second * 100))

    val chartsDir = File("charts")
    chartsDir.mkdirs()

    Variant3Charts.createTransferCostDistribution(
        players,
        File(chartsDir, "transfer_cost_distribution.png")
    )
    Variant3Charts.createTransferCostSpreadByPosition(
        players,
        File(chartsDir, "transfer_cost_by_position.png")
    )

    println()
    println("Графики сохранены в папку: ${chartsDir.absolutePath}")
}
