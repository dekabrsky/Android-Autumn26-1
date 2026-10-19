package parser

import model.Player
import model.Position
import model.Team

object CsvParser {
    fun parse(lines: Sequence<String>): ParseResult {
        val records = mutableListOf<PlayerRecord>()
        var skippedCount = 0

        lines.drop(1).forEach { line ->
            val record = parseRecordOrNull(line)

            if (record == null) {
                skippedCount++
            } else {
                records += record
            }
        }

        return ParseResult(
            players = buildPlayers(records),
            skippedCount = skippedCount,
        )
    }

    fun parse(lines: Iterable<String>): ParseResult = parse(lines.asSequence())

    fun parseRecords(lines: Sequence<String>): Sequence<PlayerRecord> =
        lines.drop(1).mapNotNull(::parseRecordOrNull)

    fun findFirstExpensiveExperiencedPlayerWithoutCards(lines: Sequence<String>): PlayerRecord? =
        parseRecords(lines).firstOrNull { record ->
            record.transferCost > 50_000_000 &&
                record.participations >= 25 &&
                record.yellowCards == 0 &&
                record.redCards == 0
        }

    private fun buildPlayers(records: List<PlayerRecord>): List<Player> {
        val teamsByNameAndCity = records
            .map { it.teamName to it.city }
            .distinct()
            .associateWith { (teamName, city) -> Team(teamName, city) }

        return records.map { record ->
            Player(
                name = record.name,
                team = teamsByNameAndCity.getValue(record.teamName to record.city),
                position = record.position,
                nationality = record.nationality,
                agency = record.agency,
                transferCost = record.transferCost,
                participations = record.participations,
                goals = record.goals,
                assists = record.assists,
                yellowCards = record.yellowCards,
                redCards = record.redCards,
            )
        }

    }

    private fun parseRecordOrNull(line: String): PlayerRecord? {
        val columns = line.split(';').map { it.trim() }
        if (columns.size != COLUMN_COUNT) return null

        val name = columns[0].takeIf { it.isNotEmpty() } ?: return null
        val teamName = columns[1].takeIf { it.isNotEmpty() } ?: return null
        val city = columns[2].takeIf { it.isNotEmpty() } ?: return null
        val position = columns[3].takeIf { it.isNotEmpty() }
            ?.let(Position::fromCsvTokenOrNull)
            ?: return null
        val nationality = columns[4].takeIf { it.isNotEmpty() } ?: return null
        val agency = columns[5].takeIf { it.isNotEmpty() }
        val transferCost = columns[6].toLongOrNull() ?: return null
        val participations = columns[7].toIntOrNull() ?: return null
        val goals = columns[8].toIntOrNull() ?: return null
        val assists = columns[9].toIntOrNull() ?: return null
        val yellowCards = columns[10].toIntOrNull() ?: return null
        val redCards = columns[11].toIntOrNull() ?: return null

        return PlayerRecord(
            name = name,
            teamName = teamName,
            city = city,
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

    private const val COLUMN_COUNT = 12
}
