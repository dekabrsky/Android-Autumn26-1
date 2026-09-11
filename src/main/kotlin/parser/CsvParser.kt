package parser

import model.Player
import model.Position
import model.Team
import java.io.InputStream

data class ParseResult(
    val players: List<Player>,
    val skippedCount: Int,
)

object CsvParser {
    private const val EXPECTED_COLUMNS = 12

    fun parse(inputStream: InputStream): ParseResult =
        inputStream.bufferedReader().use { reader ->
            parseLines(reader.readLines())
        }

    fun parse(csv: String): ParseResult =
        parseLines(csv.lineSequence().toList())

    fun parseLines(lines: List<String>): ParseResult {
        val players = mutableListOf<Player>()
        var skippedCount = 0

        lines.drop(1)
            .filter { it.isNotBlank() }
            .forEach { line ->
                val player = parsePlayerOrNull(line)
                if (player == null) {
                    skippedCount++
                } else {
                    players += player
                }
            }

        return ParseResult(players, skippedCount)
    }

    private fun parsePlayerOrNull(line: String): Player? {
        val columns = line.split(";")
        if (columns.size != EXPECTED_COLUMNS) return null

        val name = columns[0].trim().takeIf { it.isNotEmpty() } ?: return null
        val teamName = columns[1].trim().takeIf { it.isNotEmpty() } ?: return null
        val city = columns[2].trim().takeIf { it.isNotEmpty() } ?: return null
        val position = Position.fromCsvTokenOrNull(columns[3]) ?: return null
        val country = columns[4].trim().takeIf { it.isNotEmpty() } ?: return null
        val agency = columns[5].trim().takeIf { it.isNotEmpty() }
        val transferCost = columns[6].trim().toLongOrNull() ?: return null
        val participations = columns[7].trim().toIntOrNull() ?: return null
        val goals = columns[8].trim().toIntOrNull() ?: return null
        val assists = columns[9].trim().toIntOrNull() ?: return null
        val yellowCards = columns[10].trim().toIntOrNull() ?: return null
        val redCards = columns[11].trim().toIntOrNull() ?: return null

        return Player(
            name = name,
            team = Team(teamName, city),
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
}
