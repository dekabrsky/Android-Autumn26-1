import dsl.report
import model.Player
import model.Team
import parser.CsvParser
import resolver.Resolver
import visualization.visualizePositionShares
import visualization.visualizeTopTeamsStructure
import java.io.BufferedReader
import java.io.InputStreamReader

fun main(args: Array<String>) {

    System.setProperty("java.awt.headless", "true")

    println("=== Запуск футбольного аналитического модуля ===\n")

    val resourceStream = Thread.currentThread().contextClassLoader.getResourceAsStream("fakePlayersDirty.csv")
        ?: throw IllegalArgumentException("Файл fakePlayersDirty.csv не найден в папке ресурсов!")

    val lines = BufferedReader(InputStreamReader(resourceStream)).use { reader ->
        reader.lineSequence().toList()
    }

    val headerLine = lines.firstOrNull()
    val dataLines = lines.drop(1)

    val parseResult = CsvParser.parseLines(dataLines)

    println("Статистика импорта данных:")
    println("   - Всего строк обработано: ${dataLines.size}")
    println("   - Успешно импортировано игроков: ${parseResult.players.size}")
    println("   - Пропущено некорректных строк: ${parseResult.skippedCount}\n")

    val players = parseResult.players
    if (players.isEmpty()) {
        println("Нет данных для анализа. Завершение работы.")
        return
    }

    val playersWithTeamAggregates = bindPlayersToTeams(players)
    val resolver = Resolver(playersWithTeamAggregates)

    val finalReport = report {
        metric("Игроков без агента") { resolver.getCountWithoutAgency() }

        section("Атакующие показатели") {
            val (bestDefenderName, defenderGoals) = resolver.getBestScorerDefender()
            metric("Лучший бомбардир-защитник") { "$bestDefenderName ($defenderGoals голов)" }
            metric("Позиция самого дорогого немца") { resolver.getTheExpensiveGermanPlayerPosition() }
        }

        section("Грубость и дисциплина") {
            val rudestTeam = resolver.getTheRudestTeam()
            metric("Самая грубая команда") { rudestTeam.name }
        }

        section("Финансовый аудит") {
            metric("Топ-3 полезных по формуле (Гол + 2*Пас)") {
                resolver.getMostValuablePlayers().joinToString { "${it.name} (${it.goals + 2 * it.assists})" }
            }
            metric("Популярные агентства по странам") { resolver.getMostPopularAgencyByCountry() }

            val (lowShare, highShare) = resolver.getGoalsShareByMedianCost()
            metric("Доля голов игроков <= медианы цены") { String.format("%.2f%%", lowShare * 100) }
            metric("Доля голов игроков > медианы цены") { String.format("%.2f%%", highShare * 100) }
        }
    }

    println(finalReport)

    println("Генерация графических отчетов...")
    try {
        visualizePositionShares(playersWithTeamAggregates)
        println("   Базовый график сохранен: 'position_shares.png'")

        visualizeTopTeamsStructure(playersWithTeamAggregates)
        println("   Продвинутый график сохранен: 'top_5_teams_structure.png'")
    } catch (e: Exception) {
        println("Ошибка генерации графиков Kandy: ${e.message}")
        e.printStackTrace()
    }

    println("\n=== Анализ успешно завершен! ===")
}

private fun bindPlayersToTeams(rawPlayers: List<Player>): List<Player> {
    val playersByTeamName = rawPlayers.groupBy { it.team.name }
    val teamCache = playersByTeamName.mapValues { (teamName, teamPlayers) ->
        Team(name = teamName, city = teamPlayers[0].team.city, players = emptyList())
    }

    val fullTeams = playersByTeamName.mapValues { (teamName, teamPlayers) ->
        Team(name = teamName, city = teamPlayers[0].team.city, players = teamPlayers)
    }

    return rawPlayers.map { player ->
        player.copy(team = fullTeams[player.team.name] ?: player.team)
    }
}