package parser

import model.Player
import model.Team
import model.Position
import java.io.File

data class ParseResult(
    val players: List<Player>,
    val skippedCount: Int
)

class CsvParser {

    fun parse(filePath: String): ParseResult {
        val players = mutableListOf<Player>()
        var skippedCount = 0

        try {
            File(filePath).forEachLine { line ->
                val row = line.split(";").map { it.trim() }

                if (row.getOrNull(0) == "Name") {
                    return@forEachLine
                }

                if (row.size < 12 || row.all { it.isEmpty() }) {
                    skippedCount++
                    return@forEachLine
                }

                try {
                    val name = row.getOrNull(0)?.trim()
                    val teamName = row.getOrNull(1)?.trim()
                    val city = row.getOrNull(2)?.trim()
                    val positionToken = row.getOrNull(3)?.trim()
                    val nationality = row.getOrNull(4)?.trim()
                    val agency = row.getOrNull(5)?.trim()
                    val costStr = row.getOrNull(6)?.trim()
                    val participationStr = row.getOrNull(7)?.trim()
                    val goalsStr = row.getOrNull(8)?.trim()
                    val assistsStr = row.getOrNull(9)?.trim()
                    val yellowStr = row.getOrNull(10)?.trim()
                    val redStr = row.getOrNull(11)?.trim()

                    if (name.isNullOrEmpty() || teamName.isNullOrEmpty() ||
                        nationality.isNullOrEmpty() || positionToken.isNullOrEmpty()) {
                        skippedCount++
                        return@forEachLine
                    }

                    val team = Team(teamName, city)

                    val position = Position.fromCsvTokenOrNull(positionToken)
                    if (position == null) {
                        skippedCount++
                        return@forEachLine
                    }

                    val cost = parseNumber(costStr)
                    val participation = parseNumber(participationStr)
                    val goals = parseNumber(goalsStr)
                    val assists = parseNumber(assistsStr)
                    val yellow = parseNumber(yellowStr)
                    val red = parseNumber(redStr)

                    if (cost == null || participation == null || goals == null ||
                        assists == null || yellow == null || red == null) {
                        skippedCount++
                        return@forEachLine
                    }

                    val agencyValue = if (agency.isNullOrEmpty() || agency.equals("null", ignoreCase = true)) null else agency

                    val player = Player(
                        name = name,
                        team = team,
                        city = city,
                        position = position,
                        nationality = nationality,
                        agency = agencyValue,
                        transferCost = cost,
                        participation = participation,
                        goals = goals,
                        assists = assists,
                        yellowCards = yellow,
                        redCards = red
                    )

                    players.add(player)

                } catch (_: Exception) {
                    skippedCount++
                }
            }
        } catch (e: Exception) {
            println("Ошибка чтения файла: ${e.message}")
            return ParseResult(emptyList(), 0)
        }

        return ParseResult(players, skippedCount)
    }

    private fun parseNumber(value: String?): Int? {
        if (value.isNullOrEmpty()) return null

        return try {
            val cleaned = value.replace(" ", "").replace(",", ".")
            when {
                cleaned.contains(".") -> cleaned.toDouble().toInt()
                else -> cleaned.toInt()
            }
        } catch (_: NumberFormatException) {
            null
        }
    }
}