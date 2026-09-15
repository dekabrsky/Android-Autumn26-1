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
        val players = lines.drop(1)
            .mapNotNull { line ->
                if (line.isBlank()) {
                    skipped++
                    null
                } else {
                    parseLine(line) ?: run {
                        skipped++
                        null
                    }
                }
            }
            .toList()

        return ParseResult(players, skipped)
    }

    fun parse(lines: List<String>): ParseResult = parse(lines.asSequence())

    fun parseResource(resourceName: String): ParseResult {
        val stream: InputStream = requireNotNull(
            CsvParser::class.java.classLoader.getResourceAsStream(resourceName)
        ) { "Ресурс не найден на classpath: $resourceName" }
        return stream.bufferedReader().useLines(::parse)
    }

    fun parseLine(line: String): Player? {
        val tokens = line.split(DELIMITER)
        if (tokens.size < EXPECTED_COLUMNS) return null

        val name = tokens[0].trim().ifEmpty { return null }
        val teamName = tokens[1].trim().ifEmpty { return null }
        val city = tokens[2].trim().ifEmpty { return null }
        val position = Position.fromCsvTokenOrNull(tokens[3]) ?: return null
        val nationality = tokens[4].trim().ifEmpty { return null }
        val agency = tokens[5].trim().ifBlank { null }

        return Player(
            name = name,
            teamName = teamName,
            city = city,
            position = position,
            nationality = nationality,
            agency = agency,
            transferCost = tokens[6].trim().toLongOrNull() ?: return null,
            participations = tokens[7].trim().toIntOrNull() ?: return null,
            goals = tokens[8].trim().toIntOrNull() ?: return null,
            assists = tokens[9].trim().toIntOrNull() ?: return null,
            yellowCards = tokens[10].trim().toIntOrNull() ?: return null,
            redCards = tokens[11].trim().toIntOrNull() ?: return null,
        )
    }
}