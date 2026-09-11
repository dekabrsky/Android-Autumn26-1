import parser.CsvParser.readCsv
import resolver.Resolver
import visualisation.VisualisationService

fun main(args: Array<String>) {
    println("ЗАДАНИЕ 1")
    val parseResultDirty = readCsv("src/main/resources/fakePlayersDirty.csv")
    val parseResult = readCsv("src/main/resources/fakePlayers.csv")
    val players = parseResult.players
    println("Результаты парсинга fakePlayersDirty.csv")
    println("Количество игроков: ${parseResultDirty.players.size}")
    println("Количество пропусков: ${parseResultDirty.skippedCount}")
    println("Результаты парсинга fakePlayers.csv")
    println("Количество игроков: ${players.size}")
    println("Количество пропусков: ${parseResult.skippedCount}")
    println()

    println("ЗАДАНИЕ 2")
    val resolver = Resolver(players)
    println("1. Количество игроков, интересы которых не представляет агентство: ${resolver.getCountWithoutAgency()}")
    val res = resolver.getBestScorerDefender()
    println("2. Автор наибольшего числа голов среди защитников: ${res.first}; их количество: ${res.second}")
    println("3. Русское название позиции самого дорогого немецкого игрока: ${resolver.getTheExpensiveGermanPlayerPosition()}")
    println("4. Команда с наибольшим средним числом красных карточек на одного игрока: ${resolver.getTheRudestTeam().name}")
    println("5. Средняя трансферная стоимость по позициям:")
    resolver.getAverageTransferCostByPosition()
        .forEach { (position, averageCost) ->
            println("$position — ${"%.2f".format(averageCost)}")
        }
    println("6. Топ-3 самых полезных игроков:")
    resolver.getMostValuablePlayers()
        .forEachIndexed { index, player ->
            val usefulness = player.goals + 2 * player.assists
            println("(${index + 1}) ${player.name} — полезность: $usefulness")
        }
    println("7. Самое популярное агентство по каждой стране:")
    resolver.getMostPopularAgencyByCountry()
        .forEach { (country, agency) ->
            println("Страна: $country — Агентство: $agency")
        }
    println("8. Доли голов по медиане трансферной стоимости:")
    val (cheapGoalsShare, expensiveGoalsShare) = resolver.getGoalsShareByMedianCost()
    println("Игроки не дороже медианы: ${"%.2f".format(cheapGoalsShare * 100)}%")
    println("Игроки дороже медианы: ${"%.2f".format(expensiveGoalsShare * 100)}%")
    println()

    println("ЗАДАНИЕ 4 (ВАРИАНТ 4)")
    val visualisationService = VisualisationService()
    visualisationService.createCountryChart(players)
    visualisationService.createTopTeamsTransferChart(players)
}