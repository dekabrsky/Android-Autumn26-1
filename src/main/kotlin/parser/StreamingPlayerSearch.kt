package parser

import model.Player
import java.io.InputStream

object StreamingPlayerSearch {

    fun findFirstEligible(inputStream: InputStream): Player? =
        inputStream.bufferedReader().useLines { lines ->
            lines
                .drop(1)
                .mapNotNull(CsvParser::parseLine)
                .firstOrNull { player ->
                    player.transferCost > 50_000_000L &&
                            player.matches >= 25 &&
                            player.yellowCards == 0 &&
                            player.redCards == 0
                }
        }

    fun findFirstEligibleInResource(
        resourceName: String = "fakePlayersDirty.csv"
    ): Player? {
        val inputStream = javaClass.classLoader
            .getResourceAsStream(resourceName)
            ?: error("Ресурс $resourceName не найден")

        return inputStream.use(::findFirstEligible)
    }
}