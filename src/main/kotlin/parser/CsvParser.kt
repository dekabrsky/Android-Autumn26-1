package parser

import model.Player
import model.Position
import model.Team
import java.io.BufferedReader
import java.io.File
import java.io.InputStream


data class ParseResult(
    val players: List<Player>,
    val skippedCount: Int
)


object CsvParser {

    private const val EXPECTED_COLUMNS = 12

    fun parseFile(path: String): ParseResult =
        File(path).bufferedReader().use { parse(it) }


    fun parseResource(resourceName: String): ParseResult {
        val stream: InputStream = CsvParser::class.java.classLoader
            .getResourceAsStream(resourceName)
            ?: error("Resource not found on classpath: $resourceName")
        return stream.bufferedReader().use { parse(it) }
    }


    fun parse(reader: BufferedReader): ParseResult {
        val players = mutableListOf<Player>()
        var skipped = 0
        var firstLine = true

        reader.forEachLine { raw ->
            val line = raw.trim()
            if (line.isEmpty()) return@forEachLine

            if (firstLine) {
                firstLine = false
                if (line.startsWith("Name;")) return@forEachLine
            }

            val player = parseLine(line)
            if (player != null) players += player else skipped++
        }

        return ParseResult(players, skipped)
    }


    fun parseLine(line: String): Player? {
        val parts = line.split(';')
        if (parts.size < EXPECTED_COLUMNS) return null

        val name = parts[0].trim()
        val teamName = parts[1].trim()
        val city = parts[2].trim()
        val positionToken = parts[3].trim()
        val country = parts[4].trim()
        val agency = parts[5].trim().ifEmpty { null }

        val transferCost   = parts[6].trim().toIntOrNull()  ?: return null
        val participations = parts[7].trim().toIntOrNull()  ?: return null
        val goals          = parts[8].trim().toIntOrNull()  ?: return null
        val assists        = parts[9].trim().toIntOrNull()  ?: return null
        val yellowCards    = parts[10].trim().toIntOrNull() ?: return null
        val redCards       = parts[11].trim().toIntOrNull() ?: return null

        if (name.isEmpty() || teamName.isEmpty() || city.isEmpty() ||
            country.isEmpty() || positionToken.isEmpty()
        ) return null

        val position = Position.fromCsvTokenOrNull(positionToken) ?: return null

        return Player(
            name = name,
            team = Team(teamName, city),
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
