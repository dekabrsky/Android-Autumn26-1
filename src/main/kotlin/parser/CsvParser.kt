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
    fun parse(inputStream: InputStream): ParseResult {
        val players = mutableListOf<Player>()
        var skippedCount = 0

        inputStream.bufferedReader().use { reader ->
            reader.lineSequence().drop(1).forEach { line ->
                val player = parseLine(line)
                if (player == null) {
                    skippedCount++
                } else {
                    players.add(player)
                }
            }
        }

        return ParseResult(players, skippedCount)
    }

    fun parseLine(line: String): Player? {
        val columns = line.split(';')
        if (columns.size != 12) return null

        val name = columns[0].trim()
        val teamName = columns[1].trim()
        val city = columns[2].trim()
        val positionToken = columns[3].trim()
        val nationality = columns[4].trim()
        val agency = columns[5].trim().ifEmpty { null }

        if (name.isEmpty() || teamName.isEmpty() || city.isEmpty() ||
            positionToken.isEmpty() || nationality.isEmpty()
        ) {
            return null
        }

        val position = Position.fromCsvTokenOrNull(positionToken) ?: return null
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
