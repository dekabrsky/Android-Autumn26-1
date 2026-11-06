package parser

import model.Player
import model.Position
import java.io.InputStream

data class ParseResult(val players: List<Player>, val skippedCount: Int)

object CsvParser {
    private const val DELIMITER = ';'
    private const val COLUMNS = 12

    /** Разбирает одну строку. Возвращает null, если строку нужно пропустить. Не бросает исключений. */
    fun parseLine(line: String): Player? {
        val cells = line.split(DELIMITER).map { it.trim() }
        if (cells.size != COLUMNS) return null

        val name = cells[0].ifEmpty { return null }
        val team = cells[1].ifEmpty { return null }
        val city = cells[2].ifEmpty { return null }
        val position = Position.fromCsvTokenOrNull(cells[3]) ?: return null
        val country = cells[4].ifEmpty { return null }
        val agency = cells[5].ifEmpty { null } // пустое агентство — не ошибка
        val cost = cells[6].toLongOrNull()?.takeIf { it >= 0 } ?: return null
        val appearances = cells[7].toNonNegativeIntOrNull() ?: return null
        val goals = cells[8].toNonNegativeIntOrNull() ?: return null
        val assists = cells[9].toNonNegativeIntOrNull() ?: return null
        val yellow = cells[10].toNonNegativeIntOrNull() ?: return null
        val red = cells[11].toNonNegativeIntOrNull() ?: return null

        return Player(name, team, city, position, country, agency, cost, appearances, goals, assists, yellow, red)
    }

    /** Парсит последовательность строк (лениво). Полностью пустые строки игнорируются и не считаются пропущенными. */
    fun parse(lines: Sequence<String>, hasHeader: Boolean = true): ParseResult {
        val players = mutableListOf<Player>()
        var skipped = 0
        val data = if (hasHeader) lines.drop(1) else lines
        for (line in data) {
            if (line.isBlank()) continue
            val player = parseLine(line)
            if (player == null) skipped++ else players += player
        }
        return ParseResult(players, skipped)
    }

    fun parse(lines: List<String>, hasHeader: Boolean = true): ParseResult =
        parse(lines.asSequence(), hasHeader)

    fun openResource(name: String): InputStream =
        CsvParser::class.java.classLoader.getResourceAsStream(name)
            ?: error("Resource not found: $name")

    fun parseResource(name: String): ParseResult =
        openResource(name).bufferedReader().useLines { parse(it) }

    private fun String.toNonNegativeIntOrNull(): Int? = toIntOrNull()?.takeIf { it >= 0 }
}
