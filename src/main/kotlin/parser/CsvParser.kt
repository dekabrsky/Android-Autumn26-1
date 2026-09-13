package parser

import model.Player
import model.Position
import java.io.InputStream

object CsvParser {

    private const val DELIMITER = ';'
    private const val EXPECTED_COLUMNS = 12

    data class ParseResult(val players: List<Player>, val skippedCount: Int)

    fun parse(lines: Sequence<String>): ParseResult {
        var skipped = 0
        val players = mutableListOf<Player>()

        lines.drop(1).forEach { line ->
            if (line.isBlank()) {
                skipped++
                return@forEach
            }
            val player = parseLine(line)
            if (player != null) players.add(player) else skipped++
        }

        return ParseResult(players, skipped)
    }

    fun parse(lines: List<String>): ParseResult = parse(lines.asSequence())

    fun parseResource(resourceName: String): ParseResult {
        val stream: InputStream = requireNotNull(
            CsvParser::class.java.classLoader.getResourceAsStream(resourceName)
        ) { "Ресурс не найден на classpath: $resourceName" }
        return stream.bufferedReader().useLines { parse(it) }
    }

    fun parseLine(line: String): Player? {
        val tokens = line.split(DELIMITER)
        if (tokens.size < EXPECTED_COLUMNS) return null

        val name = tokens[0].trim()
        val teamName = tokens[1].trim()
        val city = tokens[2].trim()
        val position = Position.fromCsvTokenOrNull(tokens[3]) ?: return null
        val nationality = tokens[4].trim()
        val agency = tokens[5].trim().ifBlank { null }

        val transferCost = tokens[6].trim().toLongOrNull() ?: return null
        val participations = tokens[7].trim().toIntOrNull() ?: return null
        val goals = tokens[8].trim().toIntOrNull() ?: return null
        val assists = tokens[9].trim().toIntOrNull() ?: return null
        val yellowCards = tokens[10].trim().toIntOrNull() ?: return null
        val redCards = tokens[11].trim().toIntOrNull() ?: return null

        if (name.isEmpty() || teamName.isEmpty() || city.isEmpty() || nationality.isEmpty()) return null

        return Player(
            name = name,
            teamName = teamName,
            city = city,
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
