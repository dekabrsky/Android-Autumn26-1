package parser

import model.Player
import model.Position
import model.Position.Companion.fromCsvTokenOrNull
import model.Team
import java.io.InputStream

object CsvParser {

    private const val EXPECTED_COLUMNS = 12

    fun parse(inputStream: InputStream): ParseResult =
        inputStream.bufferedReader().useLines { lines ->
            val players = mutableListOf<Player>()
            var skippedCount = 0
            val teamCache = mutableMapOf<Pair<String, String>, Team>()

            // Дропнули 1 строку - оглавление
            lines.drop(1).forEach { line ->
                val player = parseLineOrNull(line, teamCache)
                if (player != null) {
                    players.add(player)
                } else {
                    skippedCount++
                }
            }

            ParseResult(players, skippedCount)
        }

    private fun parseLineOrNull(line: String, teamCache: MutableMap<Pair<String, String>, Team>): Player? {

        val parts = line
            .split(";")
            .map { it.trim() }

        if (parts.size != EXPECTED_COLUMNS) return null

        if (parts.hasEmptyAt(0, 1, 2, 4)) return null

        // Валидных строк 220 и создается много дубликатов для команд, для этого будет словарь
        val teamName = parts[1]
        val teamCity = parts[2]
        val team = teamCache.getOrPut(teamName to teamCity) {
            Team(teamName, teamCity)
        }

        team.numberOfPlayers++

        return Player(
            name = parts[0],
            team = team,
            position = fromCsvTokenOrNull(parts[3]) ?: return null,
            nationality = parts[4],
            agency = parts[5].ifEmpty { null },
            transferCost = parts[6].toLongOrNull() ?: return null,
            participations = parts[7].toIntOrNull() ?: return null,
            goals = parts[8].toIntOrNull() ?: return null,
            assists = parts[9].toIntOrNull() ?: return null,
            yellowCards = parts[10].toIntOrNull() ?: return null,
            redCards = parts[11].toIntOrNull() ?: return null,
        )
    }

    // Проверка сразу нескольких элементов на пустоту
    private fun List<String>.hasEmptyAt(vararg indices: Int): Boolean {
        return indices.any { this[it].isEmpty() }
    }
}