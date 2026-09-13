package parser

import model.Player
import model.Position
import model.Team

data class ParseResult(
    val players: List<Player>,
    val skippedCount: Int
)

class CsvParser {
    private val teamCache = mutableMapOf<Pair<String, String>, Team>()

    fun parse(lines: Sequence<String>): ParseResult {
        var skippedCount = 0
        val players = mutableListOf<Player>()

        lines.drop(1).forEach { line ->
            val player = parseLineOrNull(line)
            if (player != null) {
                players.add(player)
            } else {
                skippedCount++
            }
        }

        return ParseResult(players, skippedCount)
    }

    fun parseLineOrNull(line: String): Player? {
        val tokens = line.split(";").map { it.trim() }
        if (tokens.size < 12) return null

        val name = tokens[0].takeIf { it.isNotBlank() } ?: return null
        val teamName = tokens[1].takeIf { it.isNotBlank() } ?: return null
        val city = tokens[2].takeIf { it.isNotBlank() } ?: return null

        val position = Position.fromCsvTokenOrNull(tokens[3]) ?: return null

        val country = tokens[4].takeIf { it.isNotBlank() } ?: return null
        val agency = tokens[5].takeIf { it.isNotBlank() }

        val marketValue = tokens[6].replace(Regex("\\s+"), "").toLongOrNull() ?: return null
        val matches = tokens[7].replace(Regex("\\s+"), "").toIntOrNull() ?: return null
        val goals = tokens[8].replace(Regex("\\s+"), "").toIntOrNull() ?: return null
        val assists = tokens[9].replace(Regex("\\s+"), "").toIntOrNull() ?: return null
        val yellowCards = tokens[10].replace(Regex("\\s+"), "").toIntOrNull() ?: return null
        val redCards = tokens[11].replace(Regex("\\s+"), "").toIntOrNull() ?: return null

        val teamKey = Pair(teamName, city)
        val team = teamCache.getOrPut(teamKey) { Team(name = teamName, city = city) }

        val player = Player(
            name = name,
            team = team,
            position = position,
            country = country,
            agency = agency,
            marketValue = marketValue,
            matches = matches,
            goals = goals,
            assists = assists,
            yellowCards = yellowCards,
            redCards = redCards
        )

        team.players.add(player)

        return player
    }
}