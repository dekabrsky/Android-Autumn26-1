import model.ParseResult
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.general.DefaultPieDataset
import parser.CsvParser
import model.Player
import model.Position
import resolver.Resolver
import resolver.ByPosition
import resolver.filterBy
import java.io.File
import report.report

fun main(args: Array<String>) {
    val parseResult = runParser()
    runAllResolvers(parseResult)
    val allPlayers = parseResult.players
    if (allPlayers.isEmpty()) return

    runBaseTask(allPlayers)
    runAdvancedTask(allPlayers)
}

fun runParser() : ParseResult {
    println("Запуск парсинга")
    val parseResult = CsvParser.parse()
    println("Успешно получено ${parseResult.players.size} игроков, пропущено ${parseResult.skippedCount} строк")
    return parseResult
}

fun runAllResolvers(parseResult: ParseResult) {
    println("Запуск всех запросов")
    val resolver = Resolver(parseResult.players)
    println("1. Количество игроков без агентства: ${resolver.getCountWithoutAgency()}")
    val bestPlayer = resolver.getBestScorerDefender()
    println("2. Лучший игрок: ${bestPlayer.first} - ${bestPlayer.second} голов")
    println("3. Самый дорогой немецкий игрок: ${resolver.getTheExpensiveGermanPlayerPosition()}")
    println("4. Самая грубая команда: ${resolver.getTheRudestTeam().name}")
    println("5. Средняя стоимость по позициям:")
    val averageCost = resolver.getAverageTransferCostByPosition()
    for (cost in averageCost)
        println("\t${cost.key.russianName} - ${String.format("%.2f миллионов", cost.value / 1_000_000.0)}")
    println("6. Самые ценные игроки:")
    val mvps = resolver.getMostValuablePlayers()
    for (mvp in mvps)
        println("\t${mvp.name} - ${mvp.goals + mvp.assists * 2} очков")
    println("7. Самые популярные агентства по странам:")
    val agencies = resolver.getMostPopularAgencyByCountry()
    for (agency in agencies)
        println("\t${agency.key}: ${agency.value}")
    val goalsShare = resolver.getGoalsShareByMedianCost()
    println(
        "8. Доли голов относительно медианы: " +
                "дешевле ${"%.1f".format(goalsShare.first * 100)}%, " +
                "дороже ${"%.1f".format(goalsShare.second * 100)}%"
    )

    val defenders = parseResult.players.filterBy(ByPosition(Position.DEFENDER))
    println(report {
        metric("Без агентства") { parseResult.players.count { it.agency == null } }
        section("Атаки") {
            metric("Всего голов") { parseResult.players.sumOf { it.goals } }
        }
        metric("Защитников") { defenders.size }
    })
}

fun runBaseTask(allPlayers: List<Player>) {
    val dataset = DefaultPieDataset<String>()
    allPlayers.groupingBy { it.nationality }
        .eachCount()
        .forEach { (country, count) -> dataset.setValue(country, count.toDouble() / allPlayers.size) }

    val chart = ChartFactory.createPieChart(
        "Доля игроков по странам",
        dataset,
        true,
        true,
        false
    )
    ChartUtils.saveChartAsPNG(File("players_by_country.png"), chart, 1200, 800)
}

fun runAdvancedTask(allPlayers: List<Player>) {
    val top10Teams = allPlayers.map { it.team }
        .distinct()
        .sortedByDescending { it.totalCost }
        .take(10)
    val totalCost = allPlayers.sumOf { it.transferCost }.toDouble()
    if (totalCost == 0.0) return

    val costs = top10Teams.map { it.totalCost.toDouble() }
    val cumulativeShares = costs.runningFold(0.0, Double::plus)
        .drop(1)
        .map { it / totalCost * 100.0 }

    val dataset = DefaultCategoryDataset()
    top10Teams.zip(cumulativeShares).forEach { (team, cumulativeShare) ->
        dataset.addValue(team.totalCost / 1_000_000.0, "Стоимость состава, млн", team.name)
        dataset.addValue(cumulativeShare, "Накопленная доля, %", team.name)
    }

    val chart = ChartFactory.createBarChart(
        "Топ-10 команд и накопленная доля стоимости",
        "Команда",
        "Значение",
        dataset,
        PlotOrientation.VERTICAL,
        true,
        true,
        false
    )

    ChartUtils.saveChartAsPNG(File("teams_cumulative_cost.png"), chart, 1200, 800)
}