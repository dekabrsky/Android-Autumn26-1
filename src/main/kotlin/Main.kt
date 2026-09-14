import parser.CsvParser
import resolver.Resolver
import visualization.generateVariant2Charts
import java.io.File

fun main() {
    val file = File("src/main/resources/fakePlayersDirty.csv")
    val parseResult = CsvParser.parseCsv(file)

    println("Успешно распаршено игроков: ${parseResult.players.size}")
    println("Пропущено некорректных строк: ${parseResult.skippedCount}")

    val resolver = Resolver(parseResult.players)
    println("Игроков без агентства: ${resolver.getCountWithoutAgency()}")

    generateVariant2Charts(parseResult.players)
    println("Графики успешно сгенерированы и сохранены в корень проекта!")
}