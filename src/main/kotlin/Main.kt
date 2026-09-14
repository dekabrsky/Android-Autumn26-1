import chart.ChartData
import chart.Charts
import parser.CsvParser
import resolver.Resolver
import java.io.File

fun main() {
    val (players, skippedCount) = CsvParser.parseResource("fakePlayersDirty.csv")
    println("Загружено игроков: ${players.size}, пропущено строк: $skippedCount")
    println()

    val resolver = Resolver(players)

    println("1. Игроков без агентства: ${resolver.getCountWithoutAgency()}")

    val (defenderName, defenderGoals) = resolver.getBestScorerDefender()
    println("2. Лучший бомбардир среди защитников: $defenderName — $defenderGoals гол(ов)")

    println("3. Позиция самого дорогого немецкого игрока: ${resolver.getTheExpensiveGermanPlayerPosition()}")

    val rudestTeam = resolver.getTheRudestTeam()
    println("4. Самая грубая команда: ${rudestTeam.name} (${rudestTeam.city})")

    println("5. Средняя трансферная стоимость по позициям:")
    resolver.getAverageTransferCostByPosition().forEach { (position, cost) ->
        println("   ${position.russianName}: ${cost.formatMoney()}")
    }

    println("6. Топ-3 самых полезных игрока (голы + 2 × передачи):")
    resolver.getMostValuablePlayers().forEachIndexed { index, player ->
        val usefulness = player.goals + 2 * player.assists
        println("   ${index + 1}) ${player.name}: $usefulness (голы ${player.goals}, передачи ${player.assists})")
    }

    println("7. Самое популярное агентство по странам:")
    resolver.getMostPopularAgencyByCountry().forEach { (country, agency) ->
        println("   $country: $agency")
    }

    val (cheapShare, expensiveShare) = resolver.getGoalsShareByMedianCost()
    println("8. Доля голов игроков не дороже медианы: ${cheapShare.formatPercent()}, дороже: ${expensiveShare.formatPercent()}")
    println()

    val chartsDir = File("charts")
    Charts.save(Charts.countrySharePie(ChartData.countryShares(players)), File(chartsDir, "countries_share.png"))
    Charts.save(Charts.topTeamsPareto(ChartData.topTeamsByCost(players)), File(chartsDir, "top10_teams_cumulative.png"))
    println("Графики сохранены в ${chartsDir.absolutePath}")
}

private fun Double.formatMoney(): String = "%,.0f".format(this)

private fun Double.formatPercent(): String = "%.1f%%".format(this * 100)
