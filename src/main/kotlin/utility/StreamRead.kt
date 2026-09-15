package utility

import model.Player
import parser.CsvParser
import java.io.File

fun findFirstElitePlayer(filePath: String): Player? {
    return File(filePath).useLines { lines ->
        lines
            .drop(1)
            .mapNotNull { line ->
                CsvParser.parseLines(listOf(line)).players.firstOrNull()
            }
            .filter { player ->
                player.transferCost > 50_000_000 &&
                player.participations >= 25 &&
                player.yellowCards == 0 &&
                player.redCards == 0
            }
            .firstOrNull()
    }
}