package parser

import model.Player
import model.Position
import model.Team

data class ParseResult(
    val players: List<Player>,
    val skippedCount: Int
)

object CsvParser {

    private const val DELIMITER = ';'
    private const val EXPECTED_COLUMNS = 12

    fun parse(lines: Sequence<String>): ParseResult {
        val players = mutableListOf<Player>()
        var skipped = 0
        var headerDone = false

        for (raw in lines) {
            val line = raw.removePrefix("\uFEFF")   // снимаем BOM, если есть
            if (!headerDone) {                       // первая непустая строка — шапка
                if (line.isNotBlank()) headerDone = true
                continue
            }
            if (line.isBlank()) continue             // пустые строки игнорируем

            val player = parseLine(line)
            if (player == null) skipped++ else players.add(player)
        }
        return ParseResult(players, skipped)
    }

    /** Парсит одну строку или возвращает null, если строка битая. */
    fun parseLine(line: String): Player? {
        val cells = line.split(DELIMITER)
        if (cells.size != EXPECTED_COLUMNS) return null

        val name        = cells[0].trim()
        val teamName    = cells[1].trim()
        val city        = cells[2].trim()
        val posToken    = cells[3].trim()
        val nationality = cells[4].trim()
        val agencyRaw   = cells[5].trim()
        val costRaw     = cells[6].trim()
        val partRaw     = cells[7].trim()
        val goalsRaw    = cells[8].trim()
        val assistsRaw  = cells[9].trim()
        val yellowRaw   = cells[10].trim()
        val redRaw      = cells[11].trim()

        // Пустые обязательные текстовые поля → строка битая
        if (name.isEmpty() || teamName.isEmpty() || city.isEmpty() ||
            posToken.isEmpty() || nationality.isEmpty()) return null

        val position = Position.fromCsvTokenOrNull(posToken) ?: return null

        val transferCost = costRaw.toLongOrNull()    ?: return null
        val participations = partRaw.toIntOrNull()   ?: return null
        val goals = goalsRaw.toIntOrNull()           ?: return null
        val assists = assistsRaw.toIntOrNull()       ?: return null
        val yellowCards = yellowRaw.toIntOrNull()    ?: return null
        val redCards = redRaw.toIntOrNull()          ?: return null

        val agency = agencyRaw.ifEmpty { null }      // пусто → null

        return Player(
            name = name,
            team = Team(teamName, city),
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

    /** Удобный метод: читает ресурс из classpath. */
    fun parseResource(name: String): ParseResult {
        val stream = CsvParser::class.java.classLoader.getResourceAsStream(name)
            ?: error("Ресурс не найден: $name")
        return stream.bufferedReader().use { reader ->
            parse(reader.lineSequence())
        }
    }
}