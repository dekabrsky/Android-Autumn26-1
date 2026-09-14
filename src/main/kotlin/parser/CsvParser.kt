package parser

import model.Player
import model.Position
import model.Team
import java.io.InputStream

/** Результат парсинга: распознанные игроки и число пропущенных («битых») строк. */
data class ParseResult(
    val players: List<Player>,
    val skippedCount: Int,
)

object CsvParser {

    private const val DELIMITER = ';'
    private const val COLUMNS = 12

    // Индексы столбцов в CSV
    private const val NAME = 0
    private const val TEAM = 1
    private const val CITY = 2
    private const val POSITION = 3
    private const val NATIONALITY = 4
    private const val AGENCY = 5
    private const val TRANSFER_COST = 6
    private const val PARTICIPATIONS = 7
    private const val GOALS = 8
    private const val ASSISTS = 9
    private const val YELLOW_CARDS = 10
    private const val RED_CARDS = 11

    /** Читает CSV-ресурс из classpath (например, `fakePlayersDirty.csv`). */
    fun parseResource(resourceName: String): ParseResult {
        val stream = CsvParser::class.java.classLoader.getResourceAsStream(resourceName)
            ?: throw IllegalArgumentException("Ресурс не найден: $resourceName")
        return parse(stream)
    }

    /** Читает CSV из потока построчно, не загружая файл целиком в память. */
    fun parse(input: InputStream): ParseResult =
        input.bufferedReader().useLines { lines -> parse(lines) }

    /** Парсит строки CSV (первая строка — заголовок). */
    fun parse(lines: Sequence<String>): ParseResult {
        val players = mutableListOf<Player>()
        var skipped = 0
        lines
            .drop(1) // заголовок
            .filter { it.isNotBlank() }
            .forEach { line ->
                val player = parseLineOrNull(line)
                if (player != null) players += player else skipped++
            }
        return ParseResult(players, skipped)
    }

    /** Парсит строки CSV из списка (удобно в тестах). */
    fun parse(lines: List<String>): ParseResult = parse(lines.asSequence())

    /**
     * Преобразует одну строку CSV в [Player] по правилам маппинга.
     * Возвращает `null`, если строка некорректна и должна быть пропущена.
     */
    fun parseLineOrNull(line: String): Player? {
        val cells = line.split(DELIMITER).map { it.trim() }
        if (cells.size != COLUMNS) return null

        val name = cells[NAME].ifEmpty { return null }
        val teamName = cells[TEAM].ifEmpty { return null }
        val city = cells[CITY].ifEmpty { return null }
        val position = Position.fromCsvTokenOrNull(cells[POSITION]) ?: return null
        val nationality = cells[NATIONALITY].ifEmpty { return null }
        val agency = cells[AGENCY].ifEmpty { null }

        val transferCost = cells[TRANSFER_COST].toLongOrNull() ?: return null
        val participations = cells[PARTICIPATIONS].toIntOrNull() ?: return null
        val goals = cells[GOALS].toIntOrNull() ?: return null
        val assists = cells[ASSISTS].toIntOrNull() ?: return null
        val yellowCards = cells[YELLOW_CARDS].toIntOrNull() ?: return null
        val redCards = cells[RED_CARDS].toIntOrNull() ?: return null

        return Player(
            name = name,
            team = Team(teamName, city),
            position = position,
            nationality = nationality,
            agency = agency,
            transferCost = transferCost,
            participations = participations,
            goals = goals,
            assists = assists,
            yellowCards = yellowCards,
            redCards = redCards,
        )
    }
}
