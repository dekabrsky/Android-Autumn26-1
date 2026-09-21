package parser

import model.Player
import model.Position
import model.Team


data class ParseResult(val players: List<Player>, val skipCount: Int)

object CsvParser {
    fun parse(lines: Sequence<String>): ParseResult {
        val players = mutableListOf<Player>()
        var skipCount = 0
        val teams = mutableMapOf<Pair<String, String>, model.Team>()

        val iterator = lines.iterator()
        if (iterator.hasNext()) {
            iterator.next() // Пропускаем заголовок
        }

        while (iterator.hasNext()) {
            val line = iterator.next().trim()
            if (line.isEmpty()) {
                skipCount++
                continue
            }

            val parts = line.split(';')
            if (parts.size < 12) {
                skipCount++
                continue
            }

            val name = parts[0].trim()
            val teamName = parts[1].trim()
            val teamCity = parts[2].trim()
            val positionToken = parts[3].trim()
            val country = parts[4].trim()
            val agency = parts[5].trim().ifEmpty { null }
            val transferCost = parts[6].trim().toLongOrNull()
            val matchesCount = parts[7].trim().toIntOrNull()
            val goals = parts[8].trim().toIntOrNull()
            val assists = parts[9].trim().toIntOrNull()
            val yellowCards = parts[10].trim().toIntOrNull()
            val redCards = parts[11].trim().toIntOrNull()

            if (name.isEmpty() || teamName.isEmpty() || teamCity.isEmpty() || country.isEmpty()){
                skipCount++
                continue
            }

            val position = Position.fromCsvTokenOrNull(positionToken)
            if (position == null) {
                skipCount++
                continue
            }

            if (transferCost == null || transferCost < 0 ||
                matchesCount == null || matchesCount < 0 ||
                goals == null || goals < 0 ||
                assists == null || assists < 0 ||
                yellowCards == null || yellowCards < 0 ||
                redCards == null || redCards < 0) {
                skipCount++
                continue
            }

            val teamKey = Pair(teamName, teamCity)
            val team = teams.getOrPut(teamKey) { Team(teamName, teamCity) }

            val player = Player(
                name = name,
                team = team,
                position = position,
                country = country,
                agency = agency,
                transferCost = transferCost,
                matchesCount = matchesCount,
                goals = goals,
                assists = assists,
                yellowCards = yellowCards,
                redCards = redCards
            )
            players.add(player)
            team.addPlayer(player)
        }

        return ParseResult(players, skipCount)
    }
}