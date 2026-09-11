import parser.CsvParser
import resolver.Resolver
import visualization.PlayerCharts
import java.io.File
import java.text.NumberFormat
import java.util.Locale

fun main() {
    val inputStream = requireNotNull(
        Thread.currentThread().contextClassLoader.getResourceAsStream("fakePlayersDirty.csv"),
    ) { "Resource fakePlayersDirty.csv not found" }

    val parseResult = CsvParser.parse(inputStream)
    val players = parseResult.players
    val resolver = Resolver(players)
    val numberFormat = NumberFormat.getNumberInstance(Locale.US)

    println("Игроков успешно загружено: ${players.size}")
    println("Пропущено строк: ${parseResult.skippedCount}")
    println()

    println("1.Игроков без агентства: ${resolver.getCountWithoutAgency()}")
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
    resolver.getAverageTransferCostByPosition().forEach { (position, averageCost) ->
        println("${position.russianName}: ${numberFormat.format(averageCost)}")
    }
    println()

    println("6. Топ-3 полезных игроков:")
    resolver.getMostValuablePlayers().forEachIndexed { index, player ->
        val usefulness = player.goals + 2 * player.assists
        println("${index + 1}. ${player.name}: $usefulness")
    }
    println()

    println("7. Самое популярное агентство по странам:")
    resolver.getMostPopularAgencyByCountry().forEach { (country, agency) ->
        println("$country: $agency")
    }
    println()

    val goalsShare = resolver.getGoalsShareByMedianCost()
    println("8. Доля голов:")
    println("не дороже медианы: ${"%.2f".format(Locale.US, goalsShare.first * 100)}%")
    println("дороже медианы: ${"%.2f".format(Locale.US, goalsShare.second * 100)}%")
    println()

    val chartsDirectory = File("build/charts")
    val positionShareFile = PlayerCharts.savePositionShareChart(
        players,
        File(chartsDirectory, "position-share.png"),
    )
    val teamCompositionFile = PlayerCharts.saveTopTeamCompositionChart(
        players,
        File(chartsDirectory, "top5-team-composition.png"),
    )

}
