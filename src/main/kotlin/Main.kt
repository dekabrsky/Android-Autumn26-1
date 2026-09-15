import parser.CsvParser
import resolver.Resolver
import charts.buildCountryShareChart

fun main() {
    val result = CsvParser.parseResource("fakePlayersDirty.csv")
    println("Загружено: ${result.players.size}, пропущено: ${result.skippedCount}")

    val resolver = Resolver(result.players)

    println()
    println("1. Количество игроков, интересы которых не представляет агентство: ${resolver.getCountWithoutAgency()}")

    val (bestDefender, bestGoals) = resolver.getBestScorerDefender()
    println("2. Автор наибольшего числа голов среди защитников и их количество: $bestDefender ($bestGoals голов)")

    println("3. Русское название позиции самого дорогого немецкого игрока: " +
            resolver.getTheExpensiveGermanPlayerPosition())

    val rudest = resolver.getTheRudestTeam()
    println("4. Команда с наибольшим средним числом красных карточек на одного игрока: ${rudest.name} (${rudest.city})")

    println("5. Средняя трансферная стоимость по позициям — позиции отсортированы по убыванию средней стоимости:")
    resolver.getAverageTransferCostByPosition().forEach { (pos, avg) ->
        println("   ${pos.russianName}: ${"%.2f".format(avg)}")
    }

    println("6. Топ-3 самых полезных игроков по формуле «голы + 2 × голевые передачи» (при равенстве — раньше по алфавиту имени):")
    resolver.getMostValuablePlayers().forEach { p ->
        val utility = p.goals + 2 * p.assists
        println("   ${p.name} — ${p.team.name} (полезность $utility)")
    }

    println("7. Для каждой страны — самое популярное среди её игроков агентство (при равенстве — по алфавиту):")
    resolver.getMostPopularAgencyByCountry().forEach { (country, agency) ->
        println("   $country → $agency")
    }

    val (cheapShare, expensiveShare) = resolver.getGoalsShareByMedianCost()
    println("8. Доля голов игроков не дороже медианы: ${"%.2f".format(cheapShare)}")
    println("   Доля голов игроков дороже медианы:    ${"%.2f".format(expensiveShare)}")


    buildCountryShareChart(result.players)
}
