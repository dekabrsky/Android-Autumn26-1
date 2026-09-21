package parser

import model.Player
import java.io.InputStream
import java.io.Reader

object StreamingPlayerSearch {
    fun findFirstEligible(input: InputStream): Player? =
        input.bufferedReader().use(::findFirstEligible)

    fun findFirstEligible(reader: Reader): Player? = reader.buffered().useLines { lines ->
        lines
            .drop(1)
            .mapNotNull(CsvParser::parseLineOrNull)
            .firstOrNull { player ->
                player.transferCost > 50_000_000 &&
                    player.gamesCount >= 25 &&
                    player.yellowCardsCount == 0 &&
                    player.redCardsCount == 0
            }
    }
}
