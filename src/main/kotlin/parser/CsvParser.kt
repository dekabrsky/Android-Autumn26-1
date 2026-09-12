package parser

import model.Player
import model.Position
import model.Team
import java.io.File

object CsvParser {

    private const val COLUMN_COUNT = 12
    private const val DELIMITER = ";"

    fun parse(lines: Sequence<String>): ParseResult {
        val teams = mutableMapOf<String, Team>()
        val players = mutableListOf<Player>()
        var skipped = 0

        lines.drop(1).forEach { line ->
            if (line.isNotBlank()) {
                val player = parseLine(line, teams)
                if (player == null) skipped++ else players.add(player)
            }
        }

        return ParseResult(players, skipped)
    }

    fun parse(lines: List<String>): ParseResult = parse(lines.asSequence())

    fun parseResource(resourceName: String): ParseResult {
        val stream = requireNotNull(
            CsvParser::class.java.classLoader.getResourceAsStream(resourceName)
        ) { "Ресурс не найден: $resourceName" }

        return stream.bufferedReader(Charsets.UTF_8).useLines { parse(it) }
    }

    fun findFirstOrNull(filePath: String, predicate: (Player) -> Boolean): Player? {
        val teams = mutableMapOf<String, Team>()
        File(filePath).useLines { lines ->
            return lines.drop(1)
                .filter { it.isNotBlank() }
                .mapNotNull { parseLine(it, teams) }
                .firstOrNull(predicate)
        }
    }

    private fun parseLine(line: String, teams: MutableMap<String, Team>): Player? {
        val tokens = line.split(DELIMITER)
        if (tokens.size < COLUMN_COUNT) return null

        val name = tokens[0].trim()
        val teamName = tokens[1].trim()
        val city = tokens[2].trim()
        val positionToken = tokens[3].trim()
        val country = tokens[4].trim()
        val agencyRaw = tokens[5].trim()

        if (name.isEmpty() || teamName.isEmpty() || city.isEmpty() || country.isEmpty()) {
            return null
        }

        val position = Position.fromCsvTokenOrNull(positionToken) ?: return null
        val agency = agencyRaw.ifEmpty { null }

        val transferCost = tokens[6].trim().toLongOrNull() ?: return null
        val participations = tokens[7].trim().toIntOrNull() ?: return null
        val goals = tokens[8].trim().toIntOrNull() ?: return null
        val assists = tokens[9].trim().toIntOrNull() ?: return null
        val yellowCards = tokens[10].trim().toIntOrNull() ?: return null
        val redCards = tokens[11].trim().toIntOrNull() ?: return null

        val team = teams.getOrPut("$teamName|$city") { Team(teamName, city) }
        val player = Player(
            name = name,
            team = team,
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
        team.registerPlayer(player)
        return player
    }
}
