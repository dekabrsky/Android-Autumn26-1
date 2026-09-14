package parser

import model.*
import java.io.File

data class ParseResult(
    val players: List<Player>,
    val skippedCount: Int
)

object CsvParser {

    fun parse(filePath: String): ParseResult {
        val file = File(filePath)
        val lines = file.readLines()

        val players = mutableListOf<Player>()
        var skipped = 0

        // Пропускаем заголовок (первая строка)
        val dataLines = lines.drop(1)

        for (line in dataLines) {
            if (line.isBlank()) continue

            val player = parseLine(line)
            if (player != null) {
                players.add(player)
            } else {
                skipped++
            }
        }

        return ParseResult(players, skipped)
    }

    private fun parseLine(line: String): Player? {
        val cells = line.split(";")

        // Должно быть минимум 11 колонок
        if (cells.size < 11) return null

        val name = cells[0].trim()
        val teamName = cells[1].trim()
        val city = cells[2].trim()
        val positionToken = cells[3].trim()
        val country = cells[4].trim()
        val agencyRaw = cells[5].trim()
        val transferValueStr = cells[6].trim()
        val matchesStr = cells[7].trim()
        val goalsStr = cells[8].trim()
        val assistsStr = cells[9].trim()
        val yellowStr = cells[10].trim()
        val redStr = if (cells.size > 11) cells[11].trim() else "0"

        // Пустые обязательные текстовые поля, пропустить строку
        if (name.isEmpty() || teamName.isEmpty() || city.isEmpty() ||
            country.isEmpty() || positionToken.isEmpty()) {
            return null
        }

        // Позиция
        val position = Position.fromCsvTokenOrNull(positionToken) ?: return null

        // Агентство: пустое = null
        val agency = agencyRaw.ifEmpty { null }

        // Числовые поля: если не число, пропустить строку
        val transferValue = transferValueStr.toIntOrNull() ?: return null
        val matches = matchesStr.toIntOrNull() ?: return null
        val goals = goalsStr.toIntOrNull() ?: return null
        val assists = assistsStr.toIntOrNull() ?: return null
        val yellowCards = yellowStr.toIntOrNull() ?: return null
        val redCards = redStr.toIntOrNull() ?: return null

        return Player(
            name = name,
            team = Team(teamName, city),
            position = position,
            country = country,
            agency = agency,
            transferValue = transferValue,
            matches = matches,
            goals = goals,
            assists = assists,
            yellowCards = yellowCards,
            redCards = redCards
        )
    }
}