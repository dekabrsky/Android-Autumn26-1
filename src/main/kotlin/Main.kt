import parser.CsvParser
import resolver.Resolver
import model.Position

fun main() {
    // 1. Парсим файл
    val result = CsvParser.parse("src/main/resources/fakePlayersDirty.csv")
    val players = result.players

    println("Загружено игроков: ${players.size}")
    println("Пропущено строк: ${result.skippedCount}\n")

    // 2. Создаём резолвер
    val resolver = Resolver(players)

    // 3. Тестируем запросы
    println("1. Игроков без агентства: ${resolver.getCountWithoutAgency()}")

    val bestDefender = resolver.getBestScorerDefender()
    println("2. Лучший бомбардир-защитник: ${bestDefender.first} (${bestDefender.second} голов)")

    println("3. Позиция самого дорогого немецкого игрока: ${resolver.getTheExpensiveGermanPlayerPosition()}")

    val rudestTeam = resolver.getTheRudestTeam()
    println("4. Самая грубая команда: ${rudestTeam.name} (${rudestTeam.city})")

    println("5. Средняя стоимость по позициям:")
    resolver.getAverageTransferCostByPosition().forEach { (pos, avg) ->
        println("   - ${pos.positionName}: ${avg.toInt()}")
    }

    println("6. Топ-3 самых полезных игрока:")
    resolver.getMostValuablePlayers().forEachIndexed { index, player ->
        println("   ${index + 1}. ${player.name} (Очки: ${player.goals + 2 * player.assists})")
    }

    println("7. Самые популярные агентства по странам:")
    resolver.getMostPopularAgencyByCountry().forEach { (country, agency) ->
        println("   $country: $agency")
    }

    val (share1, share2) = resolver.getGoalsShareByMedianCost()
    println("8. Доли голов относительно медианы: дешевле ${"%.1f".format(share1 * 100)}%, дороже ${"%.1f".format(share2 * 100)}%")


    //Задание №4. Вариант 1

    // 1. Базовая задача: Круговая диаграмма
    val pieChart = org.knowm.xchart.PieChartBuilder()
        .width(800).height(600)
        .title("Распределение игроков по позициям")
        .build()

    val positionCounts = players.groupingBy { it.position }.eachCount()
    positionCounts.forEach { (position, count) ->
        pieChart.addSeries(position.positionName, count.toDouble())
    }

    org.knowm.xchart.BitmapEncoder.saveBitmap(
        pieChart,
        "position_distribution",
        org.knowm.xchart.BitmapEncoder.BitmapFormat.PNG
    )
    println("position_distribution.png создан")

    // 2. Продвинутая задача: Структура топ-5 команд (столбчатая диаграмма)
    val teamStats = players.groupBy { it.team }
        .mapValues { (team, teamPlayers) ->
            Triple(team.name, teamPlayers.sumOf { it.transferValue }.toLong(), teamPlayers)
        }
        .values
        .sortedByDescending { it.second }
        .take(5)

    val barChart = org.knowm.xchart.CategoryChartBuilder()
        .width(1000).height(600)
        .title("Структура составов топ-5 команд")
        .xAxisTitle("Команда")
        .yAxisTitle("Количество игроков")
        .build()

    // Добавляем данные по каждой позиции
    Position.values().forEach { position ->
        val xData = teamStats.map { it.first } // Названия команд
        val yData = teamStats.map { (_, _, teamPlayers) ->
            teamPlayers.count { it.position == position }.toDouble()
        }
        barChart.addSeries(position.positionName, xData, yData)
    }

    org.knowm.xchart.BitmapEncoder.saveBitmap(
        barChart,
        "top_teams_structure",
        org.knowm.xchart.BitmapEncoder.BitmapFormat.PNG
    )
    println("top_teams_structure.png создан")
}