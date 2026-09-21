import parser.CsvParser
import resolver.Resolver
import visualization.generateVariant2Charts
import java.io.File
import java.util.Locale

fun main() {
    println("Запуск парсинга")
    val file = File("src/main/resources/fakePlayersDirty.csv")
    val parseResult = CsvParser.parseCsv(file)

    println("Успешно получено ${parseResult.players.size} игроков, пропущено ${parseResult.skippedCount} строк")
    println("Запуск всех запросов")

    val resolver = Resolver(parseResult.players)

    // 1. Количество игроков без агентства
    println("1. Количество игроков без агентства: ${resolver.getCountWithoutAgency()}")

    // 2. Автор наибольшего числа голов среди защитников
    val (defenderName, defenderGoals) = resolver.getBestScorerDefender()
    println("2. Лучший игрок: $defenderName - $defenderGoals голов")

    // 3. Русское название позиции самого дорогого немецкого игрока
    println("3. Самый дорогой немецкий игрок: ${resolver.getTheExpensiveGermanPlayerPosition()}")

    // 4. Команда с наибольшим средним числом красных карточек
    val rudestTeam = resolver.getTheRudestTeam()
    println("4. Самая грубая команда: ${rudestTeam.name}")

    // 5. Средняя стоимость по позициям (в миллионах)
    println("5. Средняя стоимость по позициям:")
    val avgCostByPos = resolver.getAverageTransferCostByPosition()
    avgCostByPos.forEach { (pos, avgVal) ->
        val millions = avgVal / 1_000_000.0
        val formattedValue = String.format(Locale.GERMANY, "%.2f", millions)
        println("        ${pos.titleRu} - $formattedValue миллионов")
    }

    // 6. Топ-3 самых полезных игроков (голы + 2 * передачи)
    println("6. Самые ценные игроки:")
    val topPlayers = resolver.getMostValuablePlayers()
    topPlayers.forEach { player ->
        val points = player.goals + 2 * player.assists
        println("        ${player.name} - $points очков")
    }

    // 7. Самые популярные агентства по странам
    println("7. Самые популярные агентства по странам:")
    val topAgencies = resolver.getMostPopularAgencyByCountry()
    topAgencies.forEach { (country, agency) ->
        println("        $country: $agency")
    }

    // 8. Доли голов относительно медианы стоимости (в процентах)
    val (belowShare, aboveShare) = resolver.getGoalsShareByMedianCost()
    val formattedBelow = String.format(Locale.GERMANY, "%.1f", belowShare * 100)
    val formattedAbove = String.format(Locale.GERMANY, "%.1f", aboveShare * 100)
    println("8. Доли голов относительно медианы: дешевле $formattedBelow%, дороже $formattedAbove%")

    // Генерация графиков
    generateVariant2Charts(parseResult.players)
}
