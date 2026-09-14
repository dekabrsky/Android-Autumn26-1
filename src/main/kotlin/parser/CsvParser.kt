package parser

import model.Player
import model.Position
import model.Team
import java.io.InputStream
import java.io.Reader

data class ParseResult(
    val players: List<Player>,
    val skippedCount: Int,
)

object CsvParser {
    private const val COLUMN_COUNT = 12

    fun parse(inputStream: InputStream): ParseResult =
        inputStream.bufferedReader().use { parse(it) }

    fun parse(reader: Reader): ParseResult {
        val players = mutableListOf<Player>()
        var skippedCount = 0

        reader.buffered().useLines { lines ->
            lines.drop(1).forEach { line ->
                val player = parsePlayerOrNull(line)
                if (player == null) {
                    skippedCount++
                } else {
                    players += player
                }
            }
        }

        return ParseResult(players, skippedCount)
    }

    private fun parsePlayerOrNull(line: String): Player? {
        val columns = line.split(';')
        if (columns.size != COLUMN_COUNT) return null

        val name = columns[0].trim().takeIf { it.isNotEmpty() } ?: return null
        val teamName = columns[1].trim().takeIf { it.isNotEmpty() } ?: return null
        val city = columns[2].trim().takeIf { it.isNotEmpty() } ?: return null
        val position = Position.fromCsvTokenOrNull(columns[3]) ?: return null
        val nationality = columns[4].trim().takeIf { it.isNotEmpty() } ?: return null
        val agency = columns[5].trim().ifEmpty { null }
        val transferCost = columns[6].toLongValueOrNull() ?: return null
        val participations = columns[7].toIntValueOrNull() ?: return null
        val goals = columns[8].toIntValueOrNull() ?: return null
        val assists = columns[9].toIntValueOrNull() ?: return null
        val yellowCards = columns[10].toIntValueOrNull() ?: return null
        val redCards = columns[11].toIntValueOrNull() ?: return null

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

    private fun String.toLongValueOrNull(): Long? =
        trim().takeIf { it.isNotEmpty() }?.toLongOrNull()

    private fun String.toIntValueOrNull(): Int? =
        trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
}
