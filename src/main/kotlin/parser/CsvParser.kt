package parser

import model.Player
import model.Position
import model.Team
import java.io.InputStream
import java.io.Reader

data class ParseResult(
    val players: List<Player>,
    val skippedCount: Int
)

object CsvParser {

    fun parse(input: InputStream): ParseResult =
        input.bufferedReader().use { parse(it) }

    fun parse(reader: Reader): ParseResult =
        parseLines(reader.readLines().asSequence())

    fun parseLines(lines: Sequence<String>): ParseResult {
        val iterator = lines.iterator()
        if (!iterator.hasNext()) {
            return ParseResult(emptyList(), 0)
        }

        // Первая строка — заголовок CSV.
        iterator.next()

        val players = mutableListOf<Player>()
        var skippedCount = 0

        while (iterator.hasNext()) {
            val line = iterator.next()

            if (line.isBlank()) {
                continue
            }

            val columns = line.split(';')
            val player = mapRow(columns)

            if (player == null) {
                skippedCount++
            } else {
                players += player
            }
        }

        return ParseResult(players, skippedCount)
    }

    fun parseResource(resourceName: String): ParseResult {
        val stream = javaClass.classLoader.getResourceAsStream(resourceName)
            ?: error("Resource not found: $resourceName")

        return stream.use(::parse)
    }

    private fun mapRow(rawColumns: List<String>): Player? {
        if (rawColumns.size != 12) return null

        val columns = rawColumns.map(String::trim)

        val name = columns[0].takeIf(String::isNotEmpty) ?: return null
        val teamName = columns[1].takeIf(String::isNotEmpty) ?: return null
        val city = columns[2].takeIf(String::isNotEmpty) ?: return null
        val position = Position.fromCsvTokenOrNull(columns[3]) ?: return null
        val nationality = columns[4].takeIf(String::isNotEmpty) ?: return null

        val transferCost = columns[6].toLongOrNull() ?: return null
        val participations = columns[7].toIntOrNull() ?: return null
        val goals = columns[8].toIntOrNull() ?: return null
        val assists = columns[9].toIntOrNull() ?: return null
        val yellowCards = columns[10].toIntOrNull() ?: return null
        val redCards = columns[11].toIntOrNull() ?: return null

        val agency = columns[5].takeIf(String::isNotEmpty)

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
            redCards = redCards
        )
    }
}
