import parser.CsvParser

fun main(args: Array<String>) {
    println("Yeah rock!\n")


    val stream = CsvParser::class.java.getResourceAsStream("/fakePlayersDirty.csv") ?: error("Файл не найден")

    val result = CsvParser.parse(stream)
    println("Игроков загружено: ${result.players.size}, пропущено: ${result.skippedCount}")

}