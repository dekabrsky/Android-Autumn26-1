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
            reader.lineSequence()
                .drop(1) // пропускаем заголовок CSV
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
        val cells = line.split(';').map { it.trim() }

        if (cells.size != 12) {
            return null
        }

        val name = cells[0]
        val teamName = cells[1]
        val city = cells[2]
        val positionToken = cells[3]
        val country = cells[4]
        val agencyToken = cells[5]

        if (
            name.isEmpty() ||
            teamName.isEmpty() ||
            city.isEmpty() ||
            positionToken.isEmpty() ||
            country.isEmpty()
        ) {
            return null
        }

        val position = Position.fromCsvTokenOrNull(positionToken)
            ?: return null

        val agency = agencyToken.ifEmpty { null }

        val transferCost = cells[6].toIntOrNull()
            ?: return null

        val participations = cells[7].toIntOrNull()
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