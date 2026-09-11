import model.Player
import parser.CsvParser
import resolver.Resolver
import visualization.Variant2Charts
import java.util.Locale

fun main() {
    System.setProperty("java.awt.headless", "true")

    val parseResult = CsvParser.parseResource("fakePlayersDirty.csv")
    val players = parseResult.players
    val resolver = Resolver(players)

    println("Обработано игроков: ${players.size}; пропущено строк: ${parseResult.skippedCount}")
    println("1. Игроков без агентства: ${resolver.getCountWithoutAgency()}")

    val (defenderName, defenderGoals) = resolver.getBestScorerDefender()
    println("2. Лучший бомбардир-защитник: $defenderName — $defenderGoals голов")
    println("3. Позиция самого дорогого немецкого игрока: ${resolver.getTheExpensiveGermanPlayerPosition()}")

    val rudestTeam = resolver.getTheRudestTeam()
    println(
        "4. Команда с наибольшим средним числом красных карточек: " +
            "${rudestTeam.name} (${rudestTeam.city}), " +
            format(rudestTeam.averageRedCards),
    )

    println("5. Средняя трансферная стоимость по позициям:")
    resolver.getAverageTransferCostByPosition().forEach { (position, averageCost) ->
        println("   ${position.russianName}: ${format(averageCost)}")
    }

    println("6. Топ-3 самых полезных игроков:")
    resolver.getMostValuablePlayers().forEachIndexed { index, player ->
        println("   ${index + 1}. ${player.name}: ${usefulness(player)} баллов")
    }

    println("7. Самые популярные агентства по странам:")
    resolver.getMostPopularAgencyByCountry().forEach { (country, agency) ->
        println("   $country: $agency")
    }

    val (cheaperGoalsShare, expensiveGoalsShare) = resolver.getGoalsShareByMedianCost()
    println(
        "8. Доли голов: не дороже медианы — ${format(cheaperGoalsShare * 100)}%; " +
            "дороже — ${format(expensiveGoalsShare * 100)}%",
    )

    val streamedPlayer =
        CsvParser.findFirstExpensiveExperiencedPlayerWithoutCardsInResource("fakePlayersDirty.csv")
    println("10. Первый подходящий игрок при потоковом чтении: ${streamedPlayer?.name ?: "не найден"}")

    println("4. Вариант 2 — графики:")
    Variant2Charts.generate(players).forEach { path ->
        println("   ${path.toAbsolutePath()}")
    }
}

private fun usefulness(player: Player): Long = player.goals.toLong() + 2L * player.assists

private fun format(value: Double): String = String.format(Locale.US, "%,.2f", value)
