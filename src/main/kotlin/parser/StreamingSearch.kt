package parser

import model.Player
import java.io.Reader

fun findFirstExpensiveFairPlayer(reader: Reader, hasHeader: Boolean = true): Player? =
    reader.buffered().useLines { lines ->
        (if (hasHeader) lines.drop(1) else lines)
            .mapNotNull(CsvParser::parseLineOrNull)
            .firstOrNull {
                it.transferCost > 50_000_000 && it.participations >= 25 &&
                    it.yellowCards == 0 && it.redCards == 0
            }
    }
