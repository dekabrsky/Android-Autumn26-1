import parser.CsvParser
import resolver.Resolver
import viz.Variant1Charts
import java.io.File
import java.util.Locale
import java.io.PrintStream

fun main() {
    System.setOut(PrintStream(System.out, true, "UTF-8"))
    // 1) Парсим грязный CSV
    val result = CsvParser.parseResource("fakePlayersDirty.csv")
    val players = result.players
    println("Загружено игроков: ${players.size}")
    println("Пропущено строк:  ${result.skippedCount}")
    println()

    val resolver = Resolver(players)

    // 2) Ответы на 8 запросов
    println("1. Без агентства: ${resolver.getCountWithoutAgency()}")

    val (defName, defGoals) = resolver.getBestScorerDefender()
    println("2. Лучший защитник-бомбардир: $defName ($defGoals голов)")

    println("3. Позиция самого дорогого немца: ${resolver.getTheExpensiveGermanPlayerPosition()}")

    val rudest = resolver.getTheRudestTeam()
    println("4. Самая грубая команда: ${rudest.name} (${rudest.city})")

    println("5. Средняя стоимость по позициям:")
    resolver.getAverageTransferCostByPosition().forEach { (pos, avg) ->
        println("   ${pos.russianName}: ${fmt(avg)}")
    }

    println("6. Топ-3 полезных игроков:")
    resolver.getMostValuablePlayers().forEach {
        println("   ${it.name} — ${it.goals} + 2×${it.assists} = ${it.goals + 2 * it.assists}")
    }

    println("7. Популярное агентство по странам:")
    resolver.getMostPopularAgencyByCountry()
        .toSortedMap()
        .forEach { (country, agency) -> println("   $country → $agency") }

    val (below, above) = resolver.getGoalsShareByMedianCost()
    println("8. Доля голов игроков не дороже медианы: ${pct(below)}")
    println("   Доля голов игроков дороже медианы:     ${pct(above)}")

    // 3) Графики варианта 1
    val outDir = File("build/charts").apply { mkdirs() }
    Variant1Charts.savePositionPie(players, File(outDir, "positions.png"))
    Variant1Charts.saveTop5TeamsStacked(players, File(outDir, "top5_teams.png"))
    println()
    println("Графики сохранены в: ${outDir.absolutePath}")
}

private fun fmt(x: Double) = String.format(Locale.US, "%,.0f", x)
private fun pct(x: Double) = String.format(Locale.US, "%.1f%%", x * 100)