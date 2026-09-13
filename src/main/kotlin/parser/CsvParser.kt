package parser

import model.Player
import model.Position
import model.Team

object CsvParser {

    private const val COLUMN_COUNT = 12
    private const val DELIMITER = ";"

    fun parseResource(resourceName: String): ParseResult {
        val stream = requireNotNull(
            CsvParser::class.java.getResourceAsStream("/$resourceName")
        ) {
            "Ресурс не найден: $resourceName"
        }

        return stream.bufferedReader().useLines { lines ->
            parse(lines)
        }
    }

    fun parse(lines: Sequence<String>): ParseResult {
        val players = mutableListOf<Player>()
        val teams = mutableMapOf<Pair<String, String>, Team>()
        var skippedCount = 0

        lines.drop(1).forEach { line ->
            val parsedPlayer = parsePlayer(line)

            if (parsedPlayer == null) {
                skippedCount++
            } else {
                val team = teams.getOrPut(parsedPlayer.team.name to parsedPlayer.team.city) { parsedPlayer.team }
                val player = if (team === parsedPlayer.team) parsedPlayer else parsedPlayer.copy(team = team)
                team.addPlayer(player)
                players += player
            }
        }

        return ParseResult(players = players, skippedCount = skippedCount)
    }

    fun parsePlayer(line: String): Player? {
        val cells = line.split(DELIMITER)

        if (cells.size != COLUMN_COUNT) {
            return null
        }

        val name = cells[0].trim().takeIf { it.isNotEmpty() }
            ?: return null

        val teamName = cells[1].trim().takeIf { it.isNotEmpty() }
            ?: return null

        val city = cells[2].trim().takeIf { it.isNotEmpty() }
            ?: return null

        val position = Position.fromCsvTokenOrNull(cells[3])
            ?: return null

        val nationality = cells[4].trim().takeIf { it.isNotEmpty() }
            ?: return null

        val agency = cells[5].trim().ifEmpty { null }

        val transferCost = cells[6].trim().toLongOrNull()
            ?: return null

        val participations = cells[7].trim().toIntOrNull()
            ?: return null

        val goals = cells[8].trim().toIntOrNull()
            ?: return null

        val assists = cells[9].trim().toIntOrNull()
            ?: return null

        val yellowCards = cells[10].trim().toIntOrNull()
            ?: return null

        val redCards = cells[11].trim().toIntOrNull()
            ?: return null

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

    fun findFirstDisciplinedHighValuePlayer(resourceName: String): Player? {
        val stream = requireNotNull(
            CsvParser::class.java.getResourceAsStream("/$resourceName")
        ) {
            "Ресурс не найден: $resourceName"
        }

        return stream.bufferedReader().useLines { lines ->
            lines
                .drop(1)
                .mapNotNull { parsePlayer(it) }
                .firstOrNull { player ->
                    player.transferCost > 50_000_000L &&
                            player.participations >= 25 &&
                            player.yellowCards == 0 &&
                            player.redCards == 0
                }
        }
    }
}