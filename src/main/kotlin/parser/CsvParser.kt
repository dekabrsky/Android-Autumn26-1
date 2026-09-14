package parser

import model.Player
import model.Position
import model.Team
import java.nio.file.Files
import java.nio.file.Path

object CsvParser{
    private const val EXPECTED_COLUMNS = 12

    fun parse(lines: Sequence<String>): ParseResult {
        var skipped = 0
        val players = lines
            .drop(1) // заголовок
            .mapNotNull { line ->
                val p = parseLine(line)
                if (p == null) skipped++ else Unit
                p
            }
            .toList()
        return ParseResult(players, skipped)
    }

    fun parseFile(path: Path): ParseResult =
        Files.newBufferedReader(path).useLines { parse(it) }

    private fun parseLine(raw: String): Player? {
        val cols = raw.split(";")
        if (cols.size < EXPECTED_COLUMNS) return null


        val name = cols[0].trim().ifBlank { return null }
        val team = cols[1].trim().ifBlank { return null }
        val city = cols[2].trim().ifBlank { return null }
        val position = Position.fromCsvTokenOrNull(cols[3]) ?: return null
        val nationality = cols[4].trim().ifBlank { return null }
        val agency = cols[5].trim().ifBlank { null }
        val cost = cols[6].trim().toLongOrNull() ?: return null
        val parts = cols[7].trim().toIntOrNull() ?: return null
        val goals = cols[8].trim().toIntOrNull() ?: return null
        val assists = cols[9].trim().toIntOrNull() ?: return null
        val yellow = cols[10].trim().toIntOrNull() ?: return null
        val red = cols[11].trim().toIntOrNull() ?: return null

        return Player(
            name, Team(team, city), position, nationality, agency,
            cost, parts, goals, assists, yellow, red
        )
    }
}