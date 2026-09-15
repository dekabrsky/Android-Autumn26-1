package parser

import model.Player
import model.Position
import model.Team
import java.io.InputStream

data class ParseResult(
    val players: List<Player>,
    val skippedCount: Int
)

object CsvParser {

    private const val COLUMN_COUNT = 12

    fun parse(inputStream: InputStream): ParseResult =
        inputStream.bufferedReader().use { reader ->
            parseLines(reader.readLines())
        }

    fun parseLines(lines: List<String>): ParseResult {
        if (lines.isEmpty()) {
            return ParseResult(emptyList(), 0)
        }

        val players = mutableListOf<Player>()
        var skippedCount = 0

        lines.drop(1).forEach { line ->
            val player = parseLine(line)

            if (player == null) {
                skippedCount++
            } else {
                players += player
            }
        }

        return ParseResult(
            players = players,
            skippedCount = skippedCount
        )
    }

    fun parseLine(line: String): Player? {
        if (line.isBlank()) {
            return null
        }

        val columns = line
            .split(";")
            .map(String::trim)

        if (columns.size != COLUMN_COUNT) {
            return null
        }

        val name = columns[0]
        val teamName = columns[1]
        val city = columns[2]
        val positionToken = columns[3]
        val nationality = columns[4]
        val agencyToken = columns[5]

        // Эти текстовые поля обязательны.
        if (
            name.isEmpty() ||
            teamName.isEmpty() ||
            city.isEmpty() ||
            positionToken.isEmpty() ||
            nationality.isEmpty()
        ) {
            return null
        }

        val position =
            Position.fromCsvTokenOrNull(positionToken)
                ?: return null

        val transferCost =
            columns[6].toLongOrNull()
                ?: return null

        val participations =
            columns[7].toIntOrNull()
                ?: return null

        val goals =
            columns[8].toIntOrNull()
                ?: return null

        val assists =
            columns[9].toIntOrNull()
                ?: return null

        val yellowCards =
            columns[10].toIntOrNull()
                ?: return null

        val redCards =
            columns[11].toIntOrNull()
                ?: return null

        return Player(
            name = name,
            team = Team(
                name = teamName,
                city = city
            ),
            position = position,
            country = nationality,
            agency = agencyToken.ifEmpty { null },
            transferCost = transferCost,
            matches = participations,
            goals = goals,
            assists = assists,
            yellowCards = yellowCards,
            redCards = redCards
        )
    }
}