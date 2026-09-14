package parser

import model.ParseResult
import model.Player
import model.Position
import model.Team
import java.io.File

object CsvParser {
    fun parseCsv(file: File): ParseResult {
        var skipped = 0
        val players = mutableListOf<Player>()

        file.useLines { lines ->
            lines.drop(1).forEach { line ->
                if (line.isBlank()) {
                    skipped++
                    return@forEach
                }

                val delimiter = if (line.contains(";")) ";" else ","
                val tokens = line.split(delimiter).map { it.trim() }
                if (tokens.size < 11) {
                    skipped++
                    return@forEach
                }

                val name = tokens[0]
                val teamName = tokens[1]
                val city = tokens[2]
                val posToken = tokens[3]
                val country = tokens[4]
                val agencyRaw = tokens[5]
                val valueRaw = tokens[6]
                val matchesRaw = tokens[7]
                val goalsRaw = tokens[8]
                val assistsRaw = tokens[9]
                val yellowRaw = tokens[10]
                val redRaw = tokens.getOrNull(11) ?: "0"

                if (name.isEmpty() || teamName.isEmpty() || city.isEmpty() || country.isEmpty()) {
                    skipped++
                    return@forEach
                }

                val position = Position.fromCsvTokenOrNull(posToken) ?: run {
                    skipped++
                    return@forEach
                }

                val marketValue = valueRaw.toLongOrNull()
                val matches = matchesRaw.toIntOrNull()
                val goals = goalsRaw.toIntOrNull()
                val assists = assistsRaw.toIntOrNull()
                val yellowCards = yellowRaw.toIntOrNull()
                val redCards = redRaw.toIntOrNull()

                if (marketValue == null || matches == null || goals == null || assists == null || yellowCards == null || redCards == null) {
                    skipped++
                    return@forEach
                }

                val agency = agencyRaw.ifBlank { null }

                players.add(
                    Player(
                        name = name,
                        team = Team(teamName, city),
                        position = position,
                        country = country,
                        agency = agency,
                        marketValue = marketValue,
                        matchesPlayed = matches,
                        goals = goals,
                        assists = assists,
                        yellowCards = yellowCards,
                        redCards = redCards
                    )
                )
            }
        }

        return ParseResult(players, skipped)
    }
}