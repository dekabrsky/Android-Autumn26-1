package parser

import model.Player
import model.Position
import model.Team
import java.io.InputStream


data class ParseResult(
    val players: List<Player>,
    val skippedCount: Int,
) {
    val totalRows: Int get() = players.size + skippedCount
}
object CsvParser {

    private const val DELIMITER = ';'
    private const val COLUMN_COUNT = 12
    private const val HEADER_FIRST_CELL = "Name"

    private const val NAME = 0
    private const val TEAM = 1
    private const val CITY = 2
    private const val POSITION = 3
    private const val COUNTRY = 4
    private const val AGENCY = 5
    private const val TRANSFER_COST = 6
    private const val PARTICIPATIONS = 7
    private const val GOALS = 8
    private const val ASSISTS = 9
    private const val YELLOW_CARDS = 10
    private const val RED_CARDS = 11

    fun parse(lines: Sequence<String>): ParseResult {
        val players = mutableListOf<Player>()
        var skipped = 0
        var firstLineSeen = false

        for (line in lines) {
            if (line.isBlank()) continue
            if (!firstLineSeen) {
                firstLineSeen = true
                if (isHeader(line)) continue
            }
            val player = parseLine(line)
            if (player == null) skipped++ else players += player
        }

        return ParseResult(players, skipped)
    }

    fun parse(lines: Iterable<String>): ParseResult = parse(lines.asSequence())

    fun parse(input: InputStream): ParseResult =
        input.bufferedReader().use { reader -> parse(reader.lineSequence()) }

    fun parseResource(resourceName: String): ParseResult {
        val stream = CsvParser::class.java.classLoader.getResourceAsStream(resourceName)
            ?: error("Ресурс '$resourceName' не найден в classpath")
        return parse(stream)
    }


    fun parseLine(line: String): Player? {
        val cells = line.split(DELIMITER)
        if (cells.size != COLUMN_COUNT) return null

        val name = cells[NAME].trim().ifEmpty { return null }
        val teamName = cells[TEAM].trim().ifEmpty { return null }
        val city = cells[CITY].trim().ifEmpty { return null }
        val country = cells[COUNTRY].trim().ifEmpty { return null }
        val position = Position.fromCsvTokenOrNull(cells[POSITION]) ?: return null

        val agency = cells[AGENCY].trim().ifEmpty { null }

        val transferCost = cells[TRANSFER_COST].trim().toLongOrNull() ?: return null
        val participations = cells[PARTICIPATIONS].trim().toIntOrNull() ?: return null
        val goals = cells[GOALS].trim().toIntOrNull() ?: return null
        val assists = cells[ASSISTS].trim().toIntOrNull() ?: return null
        val yellowCards = cells[YELLOW_CARDS].trim().toIntOrNull() ?: return null
        val redCards = cells[RED_CARDS].trim().toIntOrNull() ?: return null

        return Player(
            name = name,
            team = Team(name = teamName, city = city),
            position = position,
            country = country,
            agency = agency,
            transferCost = transferCost,
            participations = participations,
            goals = goals,
            assists = assists,
            yellowCards = yellowCards,
            redCards = redCards,
        )
    }

    private fun isHeader(line: String): Boolean =
        line.substringBefore(DELIMITER).trim().equals(HEADER_FIRST_CELL, ignoreCase = true)
}
