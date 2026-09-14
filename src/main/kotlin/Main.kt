import model.Player
import model.Team
import parser.CsvParser
import parser.ParseResult
import report.report
import resolver.FilterExamples
import resolver.PlayerFilter
import resolver.Resolver
import resolver.matches
import visualization.plotCountriesShare
import visualization.plotTop10TeamsCumulativeValue
import java.util.Locale

private val RU_LOCALE = Locale("ru")

fun main() {
    println("=".repeat(60))
    println("ЗАДАЧА 1. ПАРСИНГ")
    println("=".repeat(60))

    val dirtyResult = parseAndReport(
        "Грязный файл (fakePlayersDirty.csv)",
        "/fakePlayersDirty.csv"
    )

    parseAndReport(
        "Чистый файл (fakePlayers.csv)",
        "/fakePlayers.csv"
    )

    val players = dirtyResult.players

    if (players.isEmpty()) {
        println("Нет распарсенных игроков — дальнейшие запросы бессмысленны, выходим.")
        return
    }

    println()
    println("=".repeat(60))
    println("ЗАДАЧА 2. ОБЯЗАТЕЛЬНЫЕ ЗАПРОСЫ")
    println("=".repeat(60))

    val resolver = Resolver(players)

    runRequiredQueries(resolver)

    println()
    println("=".repeat(60))
    println("ЗАДАЧА 3. ОПЦИОНАЛЬНЫЕ ЗАДАНИЯ")
    println("=".repeat(60))

    // 9. Команда-агрегат
    runTeamAggregateDemo(resolver)

    // 10. Потоковое чтение
    runStreamingSearchDemo()

    // 11. Фильтры
    runFilterDemo(players)

    // 12. DSL-отчёт
    runReportDslDemo(players)

    println()
    println("=".repeat(60))
    println("ЗАДАНИЕ 4. ВИЗУАЛИЗАЦИЯ")
    println("=".repeat(60))

    runVisualizations(players, resolver.allTeams())

    println()
    println("Готово. Проверьте png-файлы рядом с рабочей директорией проекта.")
}

private fun parseAndReport(title: String, resourcePath: String): ParseResult {
    println()
    println("--- $title ---")

    val result = CsvParser::class.java
        .getResourceAsStream(resourcePath)
        ?.use { CsvParser.parse(it) }
        ?: error("Не найден ресурс: $resourcePath")

    println("Распознано игроков: ${result.players.size}")
    println("Пропущено строк:    ${result.skippedCount}")

    return result
}

private fun runRequiredQueries(resolver: Resolver) {
    println(
        "1. Игроков без агентства: " +
                "${resolver.countPlayersWithoutAgency()}"
    )

    val topDefender = resolver.topScoringDefender()

    if (topDefender != null) {
        println(
            "\n2. Лучший бомбардир среди защитников: " +
                    "${topDefender.first.name} " +
                    "(${topDefender.first.team.displayName}), " +
                    "голов: ${topDefender.second}"
        )
    } else {
        println("\n2. Защитники не найдены.")
    }

    println(
        "\n3. Позиция самого дорогого немецкого игрока: " +
                (resolver.mostExpensiveGermanPlayerPositionName()
                    ?: "не найден")
    )

    val bestRedCardsTeam = resolver.teamWithHighestAverageRedCards()

    if (bestRedCardsTeam != null) {
        println(
            "\n4. Команда с наибольшим средним числом красных карточек: " +
                    "${bestRedCardsTeam.displayName}, " +
                    "среднее: " +
                    "%.2f".format(
                        RU_LOCALE,
                        bestRedCardsTeam.averageRedCards
                    )
        )
    } else {
        println("\n4. Команды не найдены.")
    }

    println("\n5. Средняя стоимость трансфера по позициям (по убыванию):")

    resolver.averageValueByPosition().forEach { (position, average) ->
        println(
            "   ${position.russianName}: " +
                    "%,.2f $".format(RU_LOCALE, average)
        )
    }

    println("\n6. Топ-3 игрока по формуле 'голы + 2 × передачи':")

    resolver.top3ByImpactScore().forEachIndexed { index, player ->
        println(
            "   ${index + 1}. ${player.name} — " +
                    "impact score: ${player.impactScore} " +
                    "(голов: ${player.goals}, " +
                    "передач: ${player.assists})"
        )
    }

    println("\n7. Самое популярное агентство по странам:")

    resolver.mostPopularAgencyByCountry().forEach { (country, agency) ->
        println("   $country: ${agency ?: "нет агентства"}")
    }

    val medianReport = resolver.goalsShareAroundMedianValue()

    println(
        "\n8. Медианная стоимость трансфера: " +
                "%,.0f $".format(
                    RU_LOCALE,
                    medianReport.medianValue
                )
    )

    println(
        "   Доля голов у игроков дешевле медианы: " +
                "%.1f%%".format(
                    RU_LOCALE,
                    medianReport.goalsSharePercentBelowMedian
                )
    )

    println(
        "   Доля голов у игроков дороже медианы:  " +
                "%.1f%%".format(
                    RU_LOCALE,
                    medianReport.goalsSharePercentAboveMedian
                )
    )
}

private fun runTeamAggregateDemo(resolver: Resolver) {
    println(
        "9. Команда-агрегат. Список всех игроков по командам, " +
                "общая стоимость клуба и среднее количество карточек"
    )

    val teams = resolver.allTeams()

    teams.forEachIndexed { index, team ->
        println()
        println(
            "${index + 1}) Команда - ${team.displayName} | " +
                    "Количество игроков в команде - ${team.players.size}"
        )

        println(
            "   Общая трансферная стоимость клуба: " +
                    "%,d".format(
                        RU_LOCALE,
                        team.totalSquadValue
                    )
        )

        println(
            "   Среднее количество карточек: " +
                    "%.1f".format(
                        RU_LOCALE,
                        team.averageCards
                    )
        )

        println("   Игроки находящиеся в команде:")

        println(
            "   " + team.players.joinToString("  ||  ") { player ->
                "${player.name} - " +
                        "%,d $".format(
                            RU_LOCALE,
                            player.transferValue
                        )
            }
        )
    }
}

private fun runStreamingSearchDemo() {
    println("\n10. Потоковый поиск:")

    val player = CsvParser::class.java
        .getResourceAsStream("/fakePlayers.csv")
        ?.use { CsvParser.findFirstStreaming(it) }

    if (player != null) {
        println(
            "   Первый подходящий игрок: ${player.name} " +
                    "(${player.team.displayName})"
        )
        println(
            "   Трансферная стоимость: " +
                    "%,d $".format(
                        RU_LOCALE,
                        player.transferValue
                    )
        )
        println("   Матчей: ${player.matchesPlayed}")
        println(
            "   Карточек: ${player.yellowCards + player.redCards}"
        )
    } else {
        println("   Подходящих игроков не найдено.")
    }
}

private fun runFilterDemo(players: List<Player>) {
    println("\n11. Фильтры:")

    val defenders = FilterExamples.defenders(players)

    println(
        "   Защитников в выборке (через PlayerFilter): " +
                defenders.size
    )

    val expensiveGermans = FilterExamples.expensiveGermans(players)

    println(
        "   Немецких игроков дороже 30 000 000 " +
                "(через PlayerFilter): ${expensiveGermans.size}"
    )

    val combinedFilter = PlayerFilter.And(
        PlayerFilter.Or(
            PlayerFilter.ByPosition(model.Position.MIDFIELDER),
            PlayerFilter.CheaperThan(5_000_000)
        ),
        PlayerFilter.Not(
            PlayerFilter.ByPosition(model.Position.GOALKEEPER)
        )
    )

    val combinedResult = players.filter {
        combinedFilter.matches(it)
    }

    println(
        "   Комбинированный фильтр " +
                "(полузащитники ИЛИ дешевле 5 000 000, " +
                "но не вратари): ${combinedResult.size}"
    )
}

private fun runReportDslDemo(players: List<Player>) {
    println("\n12. DSL-отчёт:")

    val result = report {
        metric("Всего игроков в выборке") {
            players.size
        }

        metric("Без агентства") {
            players.count { it.agency == null }
        }

        section("Атака") {
            metric("Всего голов") {
                players.sumOf { it.goals }
            }

            metric("Всего передач") {
                players.sumOf { it.assists }
            }
        }

        section("Цены игроков") {
            metric("Самый дорогой трансфер") {
                players.maxOfOrNull { it.transferValue }
            }

            metric("Самый дешёвый трансфер") {
                players.minOfOrNull { it.transferValue }
            }
        }
    }

    print(result)
}

private fun runVisualizations(
    players: List<Player>,
    teams: List<Team>
) {
    plotCountriesShare(
        players,
        "countries_share.png"
    )

    println("Сохранён график: countries_share.png")

    plotTop10TeamsCumulativeValue(
        players,
        "top10_teams_cumulative.png"
    )

    println("Сохранён график: top10_teams_cumulative.png")
}