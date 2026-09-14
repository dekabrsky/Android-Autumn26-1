import charts.Charts
import parser.CsvParser
import resolver.Resolver
import java.io.File

fun main(args: Array<String>) {
    val (players, skipped) = CsvParser.parseResource("fakePlayersDirty.csv")
    println("Загружено игроков: ${players.size}, пропущено строк: $skipped")
    println()

    val resolver = Resolver(players)

    println("1. Игроков без агентства: ${resolver.getCountWithoutAgency()}")

    val (defender, goals) = resolver.getBestScorerDefender()
    println("2. Лучший бомбардир среди защитников: $defender — $goals голов")

    println("3. Позиция самого дорогого немецкого игрока: ${resolver.getTheExpensiveGermanPlayerPosition()}")

    val rudest = resolver.getTheRudestTeam()
    println("4. Самая грубая команда (по красным карточкам на игрока): ${rudest.name} (${rudest.city})")

    println("5. Средняя трансферная стоимость по позициям:")
    resolver.getAverageTransferCostByPosition().forEach { (position, avg) ->
        println("   ${position.russianName.padEnd(12)} %,.0f".format(avg))
    }

    println("6. Топ-3 самых полезных игроков (голы + 2 × передачи):")
    resolver.getMostValuablePlayers().forEachIndexed { i, p ->
        println("   ${i + 1}. ${p.name} — ${p.usefulness} (голы ${p.goals}, передачи ${p.assists})")
    }

    println("7. Самое популярное агентство по странам:")
    resolver.getMostPopularAgencyByCountry().toSortedMap().forEach { (country, agency) ->
        println("   ${country.padEnd(40)} $agency")
    }

    val (cheapShare, expensiveShare) = resolver.getGoalsShareByMedianCost()
    println("8. Медиана стоимости: %,.0f".format(resolver.medianTransferCost()))
    println("   Доля голов игроков не дороже медианы: %.1f%%".format(cheapShare * 100))
    println("   Доля голов игроков дороже медианы:    %.1f%%".format(expensiveShare * 100))
    println()

    val outputDir = File(args.firstOrNull() ?: "charts")
    val files = Charts.renderAll(players, outputDir)
    println("Графики сохранены:")
    files.forEach { println("   ${it.path}") }
}
