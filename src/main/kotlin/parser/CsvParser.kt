package parser

import model.Player
import model.Position
import model.Team
import java.io.InputStream
import java.io.Reader

data class ParseResult(val players: List<Player>, val skippedCount: Int)

object CsvParser {
    private const val COLUMN_COUNT = 12

    fun parse(input: InputStream): ParseResult =
        input.bufferedReader().use(::parse)

    fun parse(reader: Reader): ParseResult =
        reader.buffered().useLines(::parse)

    fun parse(lines: Sequence<String>): ParseResult {
        val teams = mutableMapOf<Pair<String, String>, Team>()
        val players = mutableListOf<Player>()
        var skippedCount = 0

        lines.drop(1).forEach { line ->
            val player = parseLine(line) { teamName, city ->
                teams.getOrPut(teamName to city) { Team(teamName, city) }
            }
            if (player == null) {
                skippedCount++
            } else {
                players += player
            }
        }

        return ParseResult(players, skippedCount)
    }

    fun parseLineOrNull(line: String): Player? =
        parseLine(line) { teamName, city -> Team(teamName, city) }

    private fun parseLine(line: String, getTeam: (String, String) -> Team): Player? {
        val cells = line.split(';')
        if (cells.size != COLUMN_COUNT) return null

        val name = cells[0].trim().takeIf(String::isNotEmpty) ?: return null
        val teamName = cells[1].trim().takeIf(String::isNotEmpty) ?: return null
        val city = cells[2].trim().takeIf(String::isNotEmpty) ?: return null
        val positionToken = cells[3].trim().takeIf(String::isNotEmpty) ?: return null
        val position = Position.fromCsvTokenOrNull(positionToken) ?: return null
        val country = cells[4].trim().takeIf(String::isNotEmpty) ?: return null
        val agency = cells[5].trim().takeIf(String::isNotEmpty)

        return Player(
            name = name,
            team = getTeam(teamName, city),
            position = position,
            country = country,
            agency = agency,
            transferCost = cells[6].trim().toIntOrNull() ?: return null,
            gamesCount = cells[7].trim().toIntOrNull() ?: return null,
            goalsCount = cells[8].trim().toIntOrNull() ?: return null,
            goalPassesCount = cells[9].trim().toIntOrNull() ?: return null,
            yellowCardsCount = cells[10].trim().toIntOrNull() ?: return null,
            redCardsCount = cells[11].trim().toIntOrNull() ?: return null,
        )
    }
}
