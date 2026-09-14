package parser

import model.Player
import model.Position
import model.Team

data class ParseResult(
    val players: List<Player>,
    val skippedCount: Int
)

object CsvParser {

    fun parse(lines: List<String>): ParseResult {
        val players = mutableListOf<Player>()
        var skippedCount = 0

        for (line in lines.drop(1)) {
            val player = parseLine(line)

            if (player != null) {
                players.add(player)
            } else {
                skippedCount++
            }
        }

        return ParseResult(players, skippedCount)
    }

    private fun parseLine(line: String): Player? {
        val fields = line.split(";")

        if (fields.size < 12) {
            return null
        }

        val name = fields[0].trim()
        val teamName = fields[1].trim()
        val city = fields[2].trim()
        val nationality = fields[4].trim()

        if (name.isEmpty() ||
            teamName.isEmpty() ||
            city.isEmpty() ||
            nationality.isEmpty()
        ) {
            return null
        }

        val position = Position.fromCsvTokenOrNull(fields[3])
            ?: return null

        val transferCost = fields[6].trim().toLongOrNull()
            ?: return null

        val participations = fields[7].trim().toIntOrNull()
            ?: return null

        val goals = fields[8].trim().toIntOrNull()
            ?: return null

        val assists = fields[9].trim().toIntOrNull()
            ?: return null

        val yellowCards = fields[10].trim().toIntOrNull()
            ?: return null

        val redCards = fields[11].trim().toIntOrNull()
            ?: return null

        val agency = fields[5].trim().ifEmpty { null }

        return Player(
            name = name,
            team = Team(
                name = teamName,
                city = city
            ),
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