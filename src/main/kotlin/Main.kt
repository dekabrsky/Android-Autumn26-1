import parser.CsvParser
import parser.ParseResult
import resolver.Resolver
import java.nio.file.Path

fun main() {
    val path = Path.of("src/main/resources/fakePlayersDirty.csv")
    val result: ParseResult = CsvParser.parseFile(path)
    val players = result.players
    val resolver = Resolver(players)
    println("Прочитано: ${players.size}, пропущено: ${result.skipped}")

    println("1. ${resolver.getCountWithoutAgency()} - без агенства ")
    println("2. ${resolver.getBestScorerDefender()} - больше всех голов ")
    println("3. ${resolver.getTheExpensiveGermanPlayerPosition()} - позиция самого дорогого немецкого игрока ")
    val rTeam = resolver.getTheRudestTeam();
    println("4. ${rTeam.Name}: ${rTeam.City} - команда с наибольшим количеством красных карточек ")
    val avgCostByPos = resolver.getAverageTransferCostByPosition()
    println("5. Средняя трансферная стоимость по позициям ")
    for (e in avgCostByPos){
        println("   ${e.key.name}: ${e.value}")
    }
    val mostValPl = resolver.getMostValuablePlayers()
    println("6. Топ 3 игроков")
    for (e in mostValPl){
        println("   ${e.Name} - Голов забито: ${e.Goals}")
    }
    val mostAgByCountry = resolver.getMostPopularAgencyByCountry()
    println("7. Самое популярное агенство у стран ")
    for (e in mostAgByCountry){
        println("   Страна: ${e.key} - Агентство: ${e.value}")
    }
    val shares = resolver.getGoalsShareByMedianCost()
    println("8. Доли забитых голов игроками дешевле медианы - ${shares.first}, дороже - ${shares.second} ")


    Visual.playersByCountriesChart(players)
    Visual.top10TeamsByTotalCost(players)

    println("\nСтраны (топ-8 + Прочие):")
    players.groupingBy { it.Nationality }.eachCount()
        .toList().sortedByDescending { it.second }
        .take(8)
        .forEach { (c, n) ->
            println("  %-15s %4d  %5.1f%%".format(c, n, n * 100.0 / players.size))
        }

    println("\nТоп-10 команд и накопление:")
    val total = players.sumOf { it.TransferCost }.toDouble()
    val top = players.groupBy { it.Team }
        .map { (t, list) -> t to list.sumOf { it.TransferCost } }
        .sortedByDescending { it.second }
        .take(10)

    top.map { it.second }
        .runningFold(0L) { acc, c -> acc + c }
        .drop(1)
        .forEachIndexed { i, cum ->
            println("  %-20s %,12d   %5.1f%%"
                .format(top[i].first, top[i].second, cum / total * 100))
        }
}