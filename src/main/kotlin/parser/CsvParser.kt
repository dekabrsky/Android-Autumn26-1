package parser

import model.Player
import model.Position
import model.Team
import java.io.InputStream

data class ParseResult(
    val players: List<Player>,
    val skippedCount: Int
)

object CsvParser {

    fun parse(input: InputStream): ParseResult {
        val lines = input.bufferedReader().readLines()
        return parseLines(lines)
    }

    fun parse(text: String): ParseResult {
        return parseLines(text.lines())
    }

    fun parseResource(resourceName: String): ParseResult {
        val stream = javaClass.classLoader.getResourceAsStream(resourceName)
            ?: error("Resource not found: $resourceName")
        return stream.use { parse(it) }
    }

    // Задание 10: ищем первого подходящего игрока потоком, без загрузки файла целиком
    fun findFirstCleanExpensivePlayer(resourceName: String): Player? {
        val stream = javaClass.classLoader.getResourceAsStream(resourceName)
            ?: error("Resource not found: $resourceName")
        return findFirstCleanExpensivePlayer(stream)
    }

    fun findFirstCleanExpensivePlayer(stream: InputStream): Player? {
        return stream.bufferedReader().useLines { lines ->
            lines
                .drop(1)
                .mapNotNull { parseLine(it) }
                .firstOrNull { player ->
                    player.transferCost > 50_000_000 &&
                        player.participations >= 25 &&
                        player.yellowCards == 0 &&
                        player.redCards == 0
                }
        }
    }

    private fun parseLines(lines: List<String>): ParseResult {
        val players = mutableListOf<Player>()
        var skipped = 0

        // пропускаем заголовок
        for (line in lines.drop(1)) {
            if (line.isBlank()) continue

            val player = parseLine(line)
            if (player == null) {
                skipped++
            } else {
                players.add(player)
            }
        }

        return ParseResult(players, skipped)
    }

    private fun parseLine(line: String): Player? {
        val parts = line.split(';')
        if (parts.size < 12) return null

        val name = parts[0].trim()
        val teamName = parts[1].trim()
        val city = parts[2].trim()
        val positionToken = parts[3].trim()
        val nationality = parts[4].trim()
        val agencyRaw = parts[5].trim()
        val costRaw = parts[6].trim()
        val participationsRaw = parts[7].trim()
        val goalsRaw = parts[8].trim()
        val assistsRaw = parts[9].trim()
        val yellowRaw = parts[10].trim()
        val redRaw = parts[11].trim()

        // пустые обязательные текстовые поля — пропускаем строку
        if (name.isEmpty() || teamName.isEmpty() || city.isEmpty()
            || nationality.isEmpty() || positionToken.isEmpty()
        ) {
            return null
        }

        val position = Position.fromCsvTokenOrNull(positionToken) ?: return null

        val transferCost = costRaw.toLongOrNull() ?: return null
        val participations = participationsRaw.toIntOrNull() ?: return null
        val goals = goalsRaw.toIntOrNull() ?: return null
        val assists = assistsRaw.toIntOrNull() ?: return null
        val yellowCards = yellowRaw.toIntOrNull() ?: return null
        val redCards = redRaw.toIntOrNull() ?: return null

        // пустое агентство — норма, не ошибка
        val agency = agencyRaw.ifEmpty { null }

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
}
