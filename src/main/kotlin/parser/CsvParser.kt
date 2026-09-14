package parser

import model.Player
import model.Position
import model.Team
import java.io.Reader

data class ParseResult(val players: List<Player>, val skippedCount: Int)

object CsvParser {
    fun parse(reader: Reader, hasHeader: Boolean = true): ParseResult {
        val lines = reader.buffered().lineSequence()
        val dataLines = if (hasHeader) lines.drop(1) else lines
        val players = mutableListOf<Player>()
        var skippedCount = 0
        for (line in dataLines) {
            val player = parseLineOrNull(line)
            if (player == null) skippedCount++ else players.add(player)
        }
        return ParseResult(players, skippedCount)
    }

    fun parseLineOrNull(line: String): Player? {
        val cells = line.split(';').map { it.trim() }
        if (cells.size != 12) return null
        if (listOf(0, 1, 2, 4).any { cells[it].isEmpty() }) return null

        return Player(
            name = cells[0],
            team = Team(cells[1], cells[2]),
            position = Position.fromCsvTokenOrNull(cells[3]) ?: return null,
            country = cells[4],
            agency = cells[5].ifEmpty { null },
            transferCost = cells[6].toLongOrNull() ?: return null,
            participations = cells[7].toIntOrNull() ?: return null,
            goals = cells[8].toIntOrNull() ?: return null,
            assists = cells[9].toIntOrNull() ?: return null,
            yellowCards = cells[10].toIntOrNull() ?: return null,
            redCards = cells[11].toIntOrNull() ?: return null,
        )
    }
}
