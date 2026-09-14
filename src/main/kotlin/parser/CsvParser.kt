package parser

import model.Player
import model.Position
import model.Team
import java.io.InputStream

object CsvParser {

    private const val COLUMN_SEPARATOR = ";"
    private const val EXPECTED_COLUMNS = 12

    fun parse(input: InputStream): ParseResult {
        val players = mutableListOf<Player>()
        var skipped = 0

        input.bufferedReader().useLines { lines ->
            lines.drop(1).forEach { line ->
                if (line.isBlank()) return@forEach
                val player = parseLineOrNull(line)
                if (player == null) {
                    skipped++
                } else {
                    players.add(player)
                }
            }
        }

        return ParseResult(players, skipped)
    }

    private fun parseLineOrNull(line: String): Player? {
        val columns = line.split(COLUMN_SEPARATOR)
        if (columns.size != EXPECTED_COLUMNS) return null

        val name = columns[0].trim()
        val teamName = columns[1].trim()
        val city = columns[2].trim()
        val positionToken = columns[3]
        val country = columns[4].trim()
        val agencyRaw = columns[5].trim()
        val transferValueRaw = columns[6].trim()
        val matchesRaw = columns[7].trim()
        val goalsRaw = columns[8].trim()
        val assistsRaw = columns[9].trim()
        val yellowRaw = columns[10].trim()
        val redRaw = columns[11].trim()

        if (name.isEmpty() || teamName.isEmpty() || city.isEmpty() || country.isEmpty()) return null

        val position = Position.fromCsvTokenOrNull(positionToken) ?: return null

        // Пустая ячейка агентства — это null, а не ошибка данных.
        val agency = agencyRaw.ifEmpty { null }

        val transferValue = transferValueRaw.toLongOrNull() ?: return null
        val matches = matchesRaw.toIntOrNull() ?: return null
        val goals = goalsRaw.toIntOrNull() ?: return null
        val assists = assistsRaw.toIntOrNull() ?: return null
        val yellow = yellowRaw.toIntOrNull() ?: return null
        val red = redRaw.toIntOrNull() ?: return null

        val team = Team(teamName, city)

        return Player(
            name = name,
            team = team,
            position = position,
            country = country,
            agency = agency,
            transferValue = transferValue,
            matchesPlayed = matches,
            goals = goals,
            assists = assists,
            yellowCards = yellow,
            redCards = red
        )
    }

    fun findFirstStreaming(input: InputStream): Player? {
        input.bufferedReader().useLines { lines ->
            return lines
                .drop(1)
                .filter { it.isNotBlank() }
                .mapNotNull { parseLineOrNull(it) }
                .firstOrNull { player ->
                    player.transferValue > 50_000_000 &&
                            player.matchesPlayed >= 25 &&
                            player.yellowCards == 0 &&
                            player.redCards == 0
                }
        }
    }
}