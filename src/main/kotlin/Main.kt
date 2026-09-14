import parser.CsvParser

fun main() {
    val result = CsvParser.parse("src/main/resources/fakePlayersDirty.csv")

    println("Результаты парсинга")
    println("Загружено игроков: ${result.players.size}")
    println("Пропущено строк: ${result.skippedCount}")
    println()
}