package parser

import model.Player
import model.Position
import model.Team
import java.io.InputStream
import java.io.Reader

object CsvParser {

    private const val COLUMN_COUNT = 12

    fun parse(inputStream: InputStream): ParseResult {
        return inputStream
            .bufferedReader(Charsets.UTF_8)
            .use { reader ->
                parse(reader.lineSequence())
            }
    }

    fun parse(reader: Reader): ParseResult {
        return reader.buffered().use { bufferedReader ->
            parse(bufferedReader.lineSequence())
        }
    }

    fun parse(lines: Sequence<String>): ParseResult {
        val iterator = lines.iterator()

        // Пропускаем заголовок CSV.
        if (iterator.hasNext()) {
            iterator.next()
        }

        val players = mutableListOf<Player>()
        var skippedCount = 0

        while (iterator.hasNext()) {
            val player = parseLineOrNull(iterator.next())

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

    fun parseLineOrNull(line: String): Player? {
        val cells = line.split(';').map { it.trim() }

        if (cells.size != COLUMN_COUNT) {
            return null
        }

        val name = cells[0].takeIf { it.isNotEmpty() }
            ?: return null

        val teamName = cells[1].takeIf { it.isNotEmpty() }
            ?: return null

        val city = cells[2].takeIf { it.isNotEmpty() }
            ?: return null

        val position = Position.fromCsvTokenOrNull(cells[3])
            ?: return null

        val country = cells[4].takeIf { it.isNotEmpty() }
            ?: return null

        val agency = cells[5].takeIf { it.isNotEmpty() }

        val transferValue = cells[6].toLongOrNull()
            ?: return null

        val appearances = cells[7].toIntOrNull()
            ?: return null

        val goals = cells[8].toIntOrNull()
            ?: return null

        val assists = cells[9].toIntOrNull()
            ?: return null

        val yellowCards = cells[10].toIntOrNull()
            ?: return null

        val redCards = cells[11].toIntOrNull()
            ?: return null

        return Player(
            name = name,
            team = Team(teamName, city),
            position = position,
            country = country,
            agency = agency,
            transferValue = transferValue,
            appearances = appearances,
            goals = goals,
            assists = assists,
            yellowCards = yellowCards,
            redCards = redCards
        )
    }
}