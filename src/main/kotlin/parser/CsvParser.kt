package parser

import model.Player
import model.Position
import model.Team

data class ParseResult(
    val players: List<Player>,
    val skippedCount: Int,
)

object CsvParser {

    private const val DELIMITER = ';'
    private const val COLUMN_COUNT = 12

    fun parseResource(resourceName: String): ParseResult {
        val stream = javaClass.classLoader.getResourceAsStream(resourceName)
            ?: error("Ресурс $resourceName не найден")
        return stream.bufferedReader().useLines { parse(it) }
    }

    // Первая строка — заголовок. Пустые строки (например, в конце файла) не считаются пропущенными.
    fun parse(lines: Sequence<String>): ParseResult {
        val players = mutableListOf<Player>()
        var skippedCount = 0
        lines.drop(1)
            .filter { it.isNotBlank() }
            .forEach { line ->
                val player = parseLineOrNull(line)
                if (player != null) players += player else skippedCount++
            }
        return ParseResult(players, skippedCount)
    }

    fun parse(text: String): ParseResult = parse(text.lineSequence())

    // Возвращает null, если строку нужно пропустить.
    fun parseLineOrNull(line: String): Player? {
        val cells = line.split(DELIMITER).map { it.trim() }
        if (cells.size != COLUMN_COUNT) return null

        return Player(
            name = cells[0].ifEmpty { return null },
            team = Team(
                name = cells[1].ifEmpty { return null },
                city = cells[2].ifEmpty { return null },
            ),
            position = Position.fromCsvTokenOrNull(cells[3]) ?: return null,
            nationality = cells[4].ifEmpty { return null },
            agency = cells[5].ifEmpty { null },
            transferCost = cells[6].toLongOrNull()?.takeIf { it >= 0 } ?: return null,
            participations = cells[7].toNonNegativeIntOrNull() ?: return null,
            goals = cells[8].toNonNegativeIntOrNull() ?: return null,
            assists = cells[9].toNonNegativeIntOrNull() ?: return null,
            yellowCards = cells[10].toNonNegativeIntOrNull() ?: return null,
            redCards = cells[11].toNonNegativeIntOrNull() ?: return null,
        )
    }

    private fun String.toNonNegativeIntOrNull(): Int? = toIntOrNull()?.takeIf { it >= 0 }
}
