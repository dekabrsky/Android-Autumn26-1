package parser

import model.ParseResult
import model.Player
import model.Position
import model.Team
import java.io.File

object CsvParser {

    fun readCsv(path: String): ParseResult {
        val players = mutableListOf<Player>()
        var skippedCount = 0

        File(path).useLines { lines ->
            lines.drop(1)
                .forEach { line ->
                    val player = parsePlayer(line)
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

    private fun parsePlayer(line: String): Player? {
        val cells = line.split(";")
        if (cells.size != 12) {
            return null
        }

        val name = cells[0].trim()
        val teamName = cells[1].trim()
        val city = cells[2].trim()
        val position = Position.fromCsvTokenOrNull(cells[3])
        val nationality = cells[4].trim()
        val agency = cells[5].trim().ifEmpty { null }

        val transferCost = cells[6].trim().toLongOrNull() ?: return null
        val participations = cells[7].trim().toIntOrNull() ?: return null
        val goals = cells[8].trim().toIntOrNull() ?: return null
        val assists = cells[9].trim().toIntOrNull() ?: return null
        val yellowCards = cells[10].trim().toIntOrNull() ?: return null
        val redCards = cells[11].trim().toIntOrNull() ?: return null

        if (name.isEmpty() || teamName.isEmpty() || city.isEmpty() || nationality.isEmpty()
            || position == null) {
            return null
        }

        return Player(
            name, Team(teamName, city), position, nationality, agency, transferCost, participations,
            goals, assists, yellowCards, redCards)
    }
}