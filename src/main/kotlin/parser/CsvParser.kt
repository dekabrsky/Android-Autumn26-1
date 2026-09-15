package parser

import model.Player
import model.Position
import model.Team

data class ParseResult(
    val players: List<Player>,
    val skippedCount: Int
)

object CsvParser {

    fun parseLines(lines: List<String>): ParseResult {
        val players = mutableListOf<Player>()
        var skippedCount = 0

        for (line in lines) {
            if(line.blankToNull() == null) {
                skippedCount++
                continue
            }

            val tokens = line.split(";")

            if (tokens.size < 12) {
                skippedCount++
                continue
            }

            val player = mapToPlayerOrNull(tokens)
            if(player != null)
                players.add(player)
            else
                skippedCount++
        }

        return ParseResult(players, skippedCount)
    }

    private fun mapToPlayerOrNull(tokens: List<String>): Player? {
        try {
            val name = tokens[0].blankToNull() ?: return null
            val teamName = tokens[1].blankToNull() ?: return null
            val city = tokens[2].blankToNull() ?: return null
            val nationality = tokens[4].blankToNull() ?: return null

            val position = Position.fromCsvTokenOrNull(tokens[3]) ?: return null

            val agency = tokens[5].blankToNull()

            val transferCost = tokens[6].trim().toIntOrNull() ?: return null
            val participations = tokens[7].trim().toIntOrNull() ?: return null
            val goals = tokens[8].trim().toIntOrNull() ?: return null
            val assists = tokens[9].trim().toIntOrNull() ?: return null
            val yellowCards = tokens[10].trim().toIntOrNull() ?: return null
            val redCards = tokens[11].trim().toIntOrNull() ?: return null

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
        } catch (e: Exception) {
            return null
        }
    }

    private fun String.blankToNull(): String? {
        val trimmed = this.trim()
        return trimmed.ifEmpty { null }
    }
}