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

    fun parse(input: InputStream): ParseResult {
        val players = mutableListOf<Player>()
        var skippedCount = 0

        input.bufferedReader().useLines { lines ->
            lines
                .drop(1)
                .forEach { line ->
                    val player = parseLine(line)

                    if (player != null) {
                        players.add(player)
                    } else {
                        skippedCount++
                    }
                }
        }

        return ParseResult(
            players = players,
            skippedCount = skippedCount
        )
    }

    fun parseLine(line: String): Player? {
        val columns = line.split(";")

        if (columns.size != 12) {
            return null
        }

        val name = columns[0].trim()
        val teamName = columns[1].trim()
        val city = columns[2].trim()
        val positionText = columns[3].trim()
        val country = columns[4].trim()

        val agency = columns[5]
            .trim()
            .ifBlank { null }

        if (
            name.isBlank() ||
            teamName.isBlank() ||
            city.isBlank() ||
            positionText.isBlank() ||
            country.isBlank()
        ) {
            return null
        }

        val position =
            Position.fromCsvTokenOrNull(positionText)
                ?: return null

        val transferCost =
            columns[6].trim().toLongOrNull()
                ?: return null

        val participations =
            columns[7].trim().toIntOrNull()
                ?: return null

        val goals =
            columns[8].trim().toIntOrNull()
                ?: return null

        val assists =
            columns[9].trim().toIntOrNull()
                ?: return null

        val yellowCards =
            columns[10].trim().toIntOrNull()
                ?: return null

        val redCards =
            columns[11].trim().toIntOrNull()
                ?: return null

        return Player(
            name = name,
            team = Team(
                name = teamName,
                city = city
            ),
            position = position,
            country = country,
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