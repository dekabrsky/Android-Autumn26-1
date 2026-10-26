package parser

import model.Player
import model.Position
import model.Team
import java.io.File

data class ParseResult(val players: List<Player>, val skippedCount: Int)

object CsvParser {
    fun parse(lines: Sequence<String>): ParseResult {
        val players = mutableListOf<Player>()
        var skippedCount = 0

        val iterator = lines.iterator()
        if (iterator.hasNext()) {
            iterator.next()
        }

        while (iterator.hasNext()) {
            val line = iterator.next().trim()
            if (line.isEmpty()) {
                skippedCount++
                continue
            }

            val parts = line.split(';')
            if (parts.size < 12) {
                skippedCount++
                continue
            }

            val name = parts[0].trim()
            val teamName = parts[1].trim()
            val city = parts[2].trim()
            val positionToken = parts[3].trim()
            val nationality = parts[4].trim()
            val agency = parts[5].trim().let { it.ifEmpty { null } }
            val transferCostStr = parts[6].trim()
            val participationsStr = parts[7].trim()
            val goalsStr = parts[8].trim()
            val assistsStr = parts[9].trim()
            val yellowCardsStr = parts[10].trim()
            val redCardsStr = parts[11].trim()

            if (name.isEmpty() || teamName.isEmpty() || city.isEmpty() || nationality.isEmpty()) {
                skippedCount++
                continue
            }

            val position = Position.fromCsvTokenOrNull(positionToken)
            if (position == null) {
                skippedCount++
                continue
            }

            val transferCost = transferCostStr.toLongOrNull()
            val participations = participationsStr.toIntOrNull()
            val goals = goalsStr.toIntOrNull()
            val assists = assistsStr.toIntOrNull()
            val yellowCards = yellowCardsStr.toIntOrNull()
            val redCards = redCardsStr.toIntOrNull()

            if (transferCost == null || transferCost < 0 ||
                participations == null || participations < 0 ||
                goals == null || goals < 0 ||
                assists == null || assists < 0 ||
                yellowCards == null || yellowCards < 0 ||
                redCards == null || redCards < 0) {
                skippedCount++
                continue
            }

            val player = Player(
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
            players.add(player)
        }

        return ParseResult(players, skippedCount)
    }

    fun findFirstExpensiveCleanPlayer(file: File): Player? {
        return file.useLines { lines ->
            val iterator = lines.iterator()
            if (iterator.hasNext()) iterator.next()

            iterator.asSequence()
                .map { it.split(';') }
                .filter { it.size >= 12 }
                .mapNotNull { parts ->
                    try {
                        val name = parts[0].trim()
                        val teamName = parts[1].trim()
                        val city = parts[2].trim()
                        val positionToken = parts[3].trim()
                        val nationality = parts[4].trim()
                        val agency = parts[5].trim().let { it.ifEmpty { null } }
                        val transferCost = parts[6].trim().toLongOrNull() ?: return@mapNotNull null
                        val participations = parts[7].trim().toIntOrNull() ?: return@mapNotNull null
                        val goals = parts[8].trim().toIntOrNull() ?: return@mapNotNull null
                        val assists = parts[9].trim().toIntOrNull() ?: return@mapNotNull null
                        val yellowCards = parts[10].trim().toIntOrNull() ?: return@mapNotNull null
                        val redCards = parts[11].trim().toIntOrNull() ?: return@mapNotNull null

                        val position = Position.fromCsvTokenOrNull(positionToken) ?: return@mapNotNull null

                        if (name.isEmpty() || teamName.isEmpty() || city.isEmpty() || nationality.isEmpty()) return@mapNotNull null

                        // We need to create a Team. But since we are streaming, we don't have all teams.
                        // However, the requirement is just to find the player.
                        // We can create a dummy Team or use a map if we had one.
                        // But the instruction says "find the first player... without loading the whole file".
                        // So we can just return a Player with a dummy Team or a Team with just the name/city.

                        Player(
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
                    } catch (e: Exception) {
                        null
                    }
                }
                .filter {
                    it.transferCost > 50_000_000 &&
                    it.participations >= 25 &&
                    it.yellowCards == 0 &&
                    it.redCards == 0
                }
                .firstOrNull()
        }
    }
}
