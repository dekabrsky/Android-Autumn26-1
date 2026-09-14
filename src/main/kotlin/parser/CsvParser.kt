package parser

import model.Player
import model.Team
import model.Position

data class ParseResult(
    val players: List<Player>,
    val skippedCount: Int
)

object CsvParser {
    fun parse(csvLines: List<String>): ParseResult {
        val players = mutableListOf<Player>()
        var skippedCount = 0

        for ((index, line) in csvLines.withIndex()) {
            if (index == 0) continue
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
        val parts = line.split(";")

        val name = parts[0].trim()
        val teamName = parts[1].trim()
        val city = parts[2].trim()
        val positionToken = parts[3].trim()
        val country = parts[4].trim()
        val agencyRaw = parts[5].trim()
        val transferCostStr = parts[6].trim()
        val participationsStr = parts[7].trim()
        val goalsStr = parts[8].trim()
        val assistsStr = parts[9].trim()
        val yellowCardsStr = parts[10].trim()
        val redCardsStr = parts[11].trim()

        val finalAgency = agencyRaw.ifEmpty { null }
        val position = Position.fromCsvTokenOrNull(positionToken) ?: return null

        if (name.isEmpty()) return null
        if (teamName.isEmpty()) return null
        if (city.isEmpty()) return null
        if (country.isEmpty()) return null

        val transferCost = transferCostStr.toIntOrNull() ?: return null
        val participations = participationsStr.toIntOrNull() ?: return null
        val goals = goalsStr.toIntOrNull() ?: return null
        val assists = assistsStr.toIntOrNull() ?: return null
        val yellowCards = yellowCardsStr.toIntOrNull() ?: return null
        val redCards = redCardsStr.toIntOrNull() ?: return null

        return Player(
            name = name,
            team = Team(teamName, city),
            position = position,
            country = country,
            agency = finalAgency,
            transferCost = transferCost,
            participations = participations,
            goals = goals,
            assists = assists,
            yellowCards = yellowCards,
            redCards = redCards
        )
    }
}