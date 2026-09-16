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
        val parsedPlayers = mutableListOf<Player>()
        var skippedRows = 0

        inputStream.bufferedReader().use { reader ->
            reader.lineSequence()
                .drop(1)
                .forEach { row ->
                    val player = parseLine(row)

                    if (player == null) {
                        skippedRows++
                    } else {
                        parsedPlayers.add(player)
                    }
                }
        }

        return ParseResult(
            players = parsedPlayers,
            skippedCount = skippedRows
        )
    }

    fun parseLine(line: String): Player? {
        val values = line
            .split(';')
            .map { value -> value.trim() }

        if (values.size != 12) {
            return null
        }

        val name = values[0]
        val teamName = values[1]
        val city = values[2]
        val positionText = values[3]
        val country = values[4]

        if (
            name.isEmpty() ||
            teamName.isEmpty() ||
            city.isEmpty() ||
            positionText.isEmpty() ||
            country.isEmpty()
        ) {
            return null
        }

        val position =
            Position.fromCsvTokenOrNull(positionText)
                ?: return null

        val agency =
            values[5].takeIf { it.isNotEmpty() }

        val transferCost =
            values[6].toIntOrNull()
                ?: return null

        val participations =
            values[7].toIntOrNull()
                ?: return null

        val goals =
            values[8].toIntOrNull()
                ?: return null

        val assists =
            values[9].toIntOrNull()
                ?: return null

        val yellowCards =
            values[10].toIntOrNull()
                ?: return null

        val redCards =
            values[11].toIntOrNull()
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