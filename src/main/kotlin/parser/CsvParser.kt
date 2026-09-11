package parser

import model.Player
import model.Position
import model.Team
import java.io.InputStream

data class ParseResult(
    val players: List<Player>,
    val skippedCount: Int,
)

object CsvParser {
    private const val COLUMN_COUNT = 12

    fun parse(inputStream: InputStream): ParseResult =
        inputStream.bufferedReader().useLines { lines -> parse(lines) }

    fun parse(lines: Sequence<String>): ParseResult {
        val allLines = lines.toList()
        val dataLines = if (allLines.firstOrNull()?.startsWith("Name;") == true) {
            allLines.drop(1)
        } else {
            allLines
        }
        val players = mutableListOf<Player>()
        var skippedCount = 0

        dataLines.forEach { line ->
            val player = parseLineOrNull(line)
            if (player == null) {
                skippedCount++
            } else {
                players += player
            }
        }

        return ParseResult(players, skippedCount)
    }

    fun parseResource(resourceName: String): ParseResult {
        val inputStream = CsvParser::class.java.classLoader.getResourceAsStream(resourceName)
            ?: throw IllegalArgumentException("Resource not found: $resourceName")
        return parse(inputStream)
    }

    fun findFirstExpensiveExperiencedPlayerWithoutCards(inputStream: InputStream): Player? =
        inputStream.bufferedReader().useLines { lines ->
            lines
                .mapNotNull(::parseLineOrNull)
                .firstOrNull { player ->
                    player.transferCost > 50_000_000 &&
                        player.participations >= 25 &&
                        player.yellowCards == 0 &&
                        player.redCards == 0
                }
        }

    fun findFirstExpensiveExperiencedPlayerWithoutCardsInResource(
        resourceName: String,
    ): Player? {
        val inputStream = CsvParser::class.java.classLoader.getResourceAsStream(resourceName)
            ?: throw IllegalArgumentException("Resource not found: $resourceName")
        return findFirstExpensiveExperiencedPlayerWithoutCards(inputStream)
    }

    fun parseLineOrNull(line: String): Player? {
        val cells = line.split(';')
        if (cells.size != COLUMN_COUNT) return null

        val name = cells[0].trim().takeIf(String::isNotEmpty) ?: return null
        val teamName = cells[1].trim().takeIf(String::isNotEmpty) ?: return null
        val city = cells[2].trim().takeIf(String::isNotEmpty) ?: return null
        val position = Position.fromCsvTokenOrNull(cells[3]) ?: return null
        val nationality = cells[4].trim().takeIf(String::isNotEmpty) ?: return null
        val agency = cells[5].trim().takeIf(String::isNotEmpty)
        val transferCost = cells[6].trim().toIntOrNull() ?: return null
        val participations = cells[7].trim().toIntOrNull() ?: return null
        val goals = cells[8].trim().toIntOrNull() ?: return null
        val assists = cells[9].trim().toIntOrNull() ?: return null
        val yellowCards = cells[10].trim().toIntOrNull() ?: return null
        val redCards = cells[11].trim().toIntOrNull() ?: return null

        return Player(
            name = name,
            team = Team(name = teamName, city = city),
            position = position,
            nationality = nationality,
            agency = agency,
            transferCost = transferCost,
            participations = participations,
            goals = goals,
            assists = assists,
            yellowCards = yellowCards,
            redCards = redCards,
        )
    }
}
