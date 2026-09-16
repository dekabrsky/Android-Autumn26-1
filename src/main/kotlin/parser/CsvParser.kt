package parser

import model.Player
import model.Position
import model.Team
import java.io.InputStream
import java.io.Reader
import java.nio.charset.StandardCharsets

object CsvParser {

    fun parse(input: InputStream): ParseResult =
        parse(input.bufferedReader(StandardCharsets.UTF_8))

    fun parse(reader: Reader): ParseResult {
        val players = mutableListOf<Player>()
        var skipped = 0
        var delimiter: Char? = null

        reader.useLines { lines ->
            for (raw in lines) {
                if (raw.isBlank()) continue
                if (delimiter == null) {
                    delimiter = detectDelimiter(raw)
                    continue
                }
                val player = parseLine(raw, delimiter!!)
                if (player == null) skipped++ else players += player
            }
        }
        return ParseResult(players, skipped)
    }

    private fun detectDelimiter(header: String): Char {
        var comma = 0; var semi = 0; var inQuotes = false
        for (c in header) {
            when {
                c == '"' -> inQuotes = !inQuotes
                !inQuotes && c == ',' -> comma++
                !inQuotes && c == ';' -> semi++
            }
        }
        return if (semi > comma) ';' else ','
    }

    private fun parseLine(line: String, delimiter: Char): Player? {
        val cols = splitCsv(line, delimiter)
        if (cols.size < 12) return null

        val name        = cols[0].trim().takeIf { it.isNotEmpty() } ?: return null
        val teamName    = cols[1].trim().takeIf { it.isNotEmpty() } ?: return null
        val teamCity    = cols[2].trim().takeIf { it.isNotEmpty() } ?: return null
        val positionRaw = cols[3].trim().takeIf { it.isNotEmpty() } ?: return null
        val country     = cols[4].trim().takeIf { it.isNotEmpty() } ?: return null
        val agency      = cols[5].trim().takeIf { it.isNotEmpty() }

        val transferValue = parseLong(cols[6])  ?: return null
        val matches       = parseInt(cols[7])   ?: return null
        val goals         = parseInt(cols[8])   ?: return null
        val assists       = parseInt(cols[9])   ?: return null
        val yellowCards   = parseInt(cols[10])  ?: return null
        val redCards      = parseInt(cols[11])  ?: return null

        val position = Position.fromCsvTokenOrNull(positionRaw) ?: return null

        return Player(
            name = name,
            team = Team(name = teamName, city = teamCity),
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

    private fun parseLong(s: String): Long? =
        s.trim().replace("\u00A0", "").replace(" ", "").toLongOrNull()

    private fun parseInt(s: String): Int? =
        s.trim().replace("\u00A0", "").replace(" ", "").toIntOrNull()

    private fun splitCsv(line: String, delimiter: Char): List<String> {
        val result = ArrayList<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                inQuotes -> {
                    if (c == '"') {
                        if (i + 1 < line.length && line[i + 1] == '"') { sb.append('"'); i++ }
                        else inQuotes = false
                    } else sb.append(c)
                }
                c == '"' -> inQuotes = true
                c == delimiter -> { result += sb.toString(); sb.setLength(0) }
                else -> sb.append(c)
            }
            i++
        }
        result += sb.toString()
        return result
    }
}