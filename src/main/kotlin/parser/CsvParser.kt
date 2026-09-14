package parser

import model.Player
import model.Position
import model.Team
import java.io.InputStream

fun parsePlayer(line: String): Player? {
    val values = line.split(";")

    if (values.size != 12) return null

    val name = values[0].trim()
    val teamName = values[1].trim()
    val city = values[2].trim()
    val positionToken = values[3].trim()
    val nationality = values[4].trim()
    val agencyToken = values[5].trim()

    if (
        name.isEmpty() ||
        teamName.isEmpty() ||
        city.isEmpty() ||
        nationality.isEmpty() ||
        positionToken.isEmpty()
    ) {
        return null
    }

    val position = Position.fromCsvTokenOrNull(positionToken)
        ?: return null

    val transferCost = values[6].trim().toIntOrNull()
        ?: return null
    val participations = values[7].trim().toIntOrNull()
        ?: return null
    val goals = values[8].trim().toIntOrNull()
        ?: return null
    val assists = values[9].trim().toIntOrNull()
        ?: return null
    val yellowCards = values[10].trim().toIntOrNull()
        ?: return null
    val redCards = values[11].trim().toIntOrNull()
        ?: return null

    return Player(
        name = name,
        team = Team(teamName, city),
        position = position,
        nationality = nationality,
        agency = agencyToken.ifEmpty { null },
        transferCost = transferCost,
        participations = participations,
        goals = goals,
        assists = assists,
        yellowCards = yellowCards,
        redCards = redCards
    )
}

fun readCSV(inputStream: InputStream): ParseResult {
    val players = mutableListOf<Player>()
    var skippedCount = 0

    inputStream.bufferedReader().useLines { lines ->
        lines
            .drop(1)
            .forEach { line ->
                val player = parsePlayer(line)

                if (player == null) {
                    skippedCount++
                } else {
                    players.add(player)
                }
            }
    }

    return ParseResult(players, skippedCount)
}

data class ParseResult(
    val players: List<Player>,
    val skippedCount: Int,
)