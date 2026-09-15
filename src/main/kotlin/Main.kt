import parser.CsvParser
import visual.ChartBuilder

fun main(args: Array<String>) {
    val csv = object {}.javaClass
        .getResource("/fakePlayers.csv")!!
        .readText()

    val result = CsvParser.parser(csv)

    //ChartBuilder.showTransferCostDistribution(result.players)
    ChartBuilder.showTransferCostByPosition(result.players)
}