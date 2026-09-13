import charts.Charts
import filter.PlayerFilter
import filter.filterBy
import model.Position
import parser.CsvParser
import resolver.Resolver

fun main() {
    val result = CsvParser.parseResource("fakePlayersDirty.csv")
    val players = result.players
    val resolver = Resolver(players)

    println("Обработано игроков: ${players.size}")
    println("Кол-во пропущенных строк: ${result.skippedCount}")
    println()

    println("1. Без агентсва: ${resolver.getCountWithoutAgency()}")

    val bestDefender = resolver.getBestScorerDefender()
    println("2. Лучший голеадор среди защитников: ${bestDefender.first} (${bestDefender.second} голов)")

    println("3. Позиция самого дорогого немца: ${resolver.getTheExpensiveGermanPlayerPosition()}")

    val rudest = resolver.getTheRudestTeam()
    println(
        "4. Самая грубая команда: ${rudest.name} (${rudest.city}), " +
            "среднее красных: ${"%.2f".format(rudest.averageRedCards)}"
    )

    println("5. Средняя стоимость по позициям:")
    resolver.getAverageTransferCostByPosition().forEach { (pos, avg) ->
        println("   ${pos.russianName}: ${"%.0f".format(avg)}")
    }

    println("6. Топ-3 полезных игроков:")
    resolver.getMostValuablePlayers().forEachIndexed { i, p ->
        val score = p.goals + 2 * p.assists
        println("   ${i + 1}. ${p.name} (полезность=$score)")
    }

    println("7. Популярные агентства по странам:")
    resolver.getMostPopularAgencyByCountry().entries
        .sortedBy { it.key }
        .take(10)
        .forEach { (country, agency) ->
            println("   $country -> $agency")
        }
    println("   ...")

    val shares = resolver.getGoalsShareByMedianCost()
    println(
        "8. Доля голов: не дороже медианы = ${"%.3f".format(shares.first)}, " +
            "дороже = ${"%.3f".format(shares.second)}"
    )

    println()
    val streamed = CsvParser.findFirstCleanExpensivePlayer("fakePlayersDirty.csv")
    println(
        "10. Первый дорогой «чистый» игрок: " +
            (streamed?.let { "${it.name}, стоимость=${it.transferCost}, матчи=${it.participations}" }
                ?: "не найден")
    )

    // 11. пример составного фильтра: защитники дешевле 40 млн
    val defendersUnder40 = PlayerFilter.And(
        PlayerFilter.ByPosition(Position.DEFENDER),
        PlayerFilter.CheaperThan(40_000_000)
    )
    val filtered = players.filterBy(defendersUnder40)
    println("11. Защитники дешевле 40 млн: ${filtered.size} чел.")

    println()
    Charts.saveAll(players)
}
