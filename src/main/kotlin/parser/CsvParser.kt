package parser

import java.io.InputStream
import java.io.Reader
import java.nio.file.Path
import kotlin.io.path.reader
import model.Player
import model.Position
import model.Team

data class ParseResult(
    val players: List<Player>,
    val skippedCount: Int,
)

data class HighValueDisciplinedPlayers(
    val playerWithoutRedCards: Player?,
    val playerWithoutYellowCards: Player?,
    val playerWithoutAnyCards: Player?
)

object CsvParser {
    private const val FIELD_COUNT = 12

    fun parse(lines: Iterable<String>): ParseResult = parse(lines.asSequence())

    fun parse(input: InputStream): ParseResult = input.bufferedReader().use { parse(it) }

    fun parse(path: Path): ParseResult = path.reader().use { parse(it) }

    private fun parse(reader: Reader): ParseResult = reader.use { parse(it.readLines()) }

    fun parse(lines: Sequence<String>): ParseResult {
        val iterator = lines.iterator()
        if (!iterator.hasNext()) return ParseResult(emptyList(), 0)

        iterator.next()
        val players = mutableListOf<Player>()
        val teams = mutableMapOf<Pair<String, String>, Team>()
        var skippedCount = 0

        iterator.forEachRemaining { line ->
            val parsedPlayer = parseRecord(line)
            if (parsedPlayer == null) {
                skippedCount++
            } else {
                val team = teams.getOrPut(parsedPlayer.team.name to parsedPlayer.team.city) { parsedPlayer.team }
                val player = if (team === parsedPlayer.team) parsedPlayer else parsedPlayer.copy(team = team)
                team.addPlayer(player)
                players += player
            }
        }
        return ParseResult(players, skippedCount)
    }

    fun findHighValueDisciplinedPlayers (input: InputStream): HighValueDisciplinedPlayers {
        var firstPlayerWithoutRedCards: Player? = null
        var firstPlayerWithoutYellowCards: Player? = null
        var firstPlayerWithoutAnyCards: Player? = null

        input.bufferedReader().useLines { lines ->
            lines.drop(1)
                .mapNotNull(::parseRecord)
                .filter { it.transferCost > 50_000_000 && it.participations >= 25 }
                .forEach { player ->
                    if (firstPlayerWithoutRedCards == null && player.redCards == 0) {
                        firstPlayerWithoutRedCards = player
                    }
                    if (firstPlayerWithoutYellowCards == null && player.yellowCards == 0) {
                        firstPlayerWithoutYellowCards = player
                    }
                    if (firstPlayerWithoutAnyCards == null && player.yellowCards == 0 && player.redCards == 0) {
                        firstPlayerWithoutAnyCards = player
                    }
                }
        }

        return HighValueDisciplinedPlayers(firstPlayerWithoutRedCards, firstPlayerWithoutYellowCards, firstPlayerWithoutAnyCards)
    }

    fun parseRecord(line: String): Player? {
        val fields = line.split(';')
        if (fields.size != FIELD_COUNT) return null

        val name = fields[0].trim().takeIf(String::isNotEmpty) ?: return null
        val teamName = fields[1].trim().takeIf(String::isNotEmpty) ?: return null
        val city = fields[2].trim().takeIf(String::isNotEmpty) ?: return null
        val position = Position.fromCsvTokenOrNull(fields[3]) ?: return null
        val nationality = fields[4].trim().takeIf(String::isNotEmpty) ?: return null
        val agency = fields[5].trim().takeIf(String::isNotEmpty)

        val transferCost = fields[6].trim().toIntOrNull() ?: return null
        val participations = fields[7].trim().toIntOrNull() ?: return null
        val goals = fields[8].trim().toIntOrNull() ?: return null
        val assists = fields[9].trim().toIntOrNull() ?: return null
        val yellowCards = fields[10].trim().toIntOrNull() ?: return null
        val redCards = fields[11].trim().toIntOrNull() ?: return null

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
