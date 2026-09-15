package streaming

import model.Player
import parser.CsvParser
import java.io.File


object CsvStreaming {

    fun findFirstExpensiveCleanPlayer(filePath: String): Player? =
        File(filePath).useLines { lines ->
            lines
                .drop(1)
                .mapNotNull(CsvParser::parseLine)
                .firstOrNull { player ->
                    player.transferCost > 50_000_000 &&
                            player.participations >= 25 &&
                            player.yellowCards == 0 &&
                            player.redCards == 0
                }
        }
}