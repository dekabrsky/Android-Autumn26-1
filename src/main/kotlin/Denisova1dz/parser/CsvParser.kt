package Denisova1dz.parser

import Denisova1dz.model.Player
import Denisova1dz.model.Position
import Denisova1dz.model.Team
import java.io.File


//players — успешно прочитанные игроки,
//skippedCount — сколько строк не удалось разобрать.

data class ParseResult(
    val players: List<Player>,
    val skippedCount: Int
)

object CsvParser {

    // точка с запятой является разделителем в файле
    private const val DELIMITER = ";"

    // В строке должно быть 12 колонок
    private const val COLUMNS = 12

    //читаем файл и разбираем его
    fun parse(path: String): ParseResult {
        // читаем все строки файла
        val lines = File(path).readLines()

        // список для игроков и счётчик пропущенных
        val players = mutableListOf<Player>()
        var skipped = 0

        // идём по строкам
        for (line in lines) {

            // заголовок пропускаем (не игрок и не ошибка)
            if (line.startsWith("Name", ignoreCase = true)) continue

            // пустые строки тоже пропускаем
            if (line.isBlank()) continue

            // превращаем строку в Player
            // если получилось, то player не null, если нет, то null
            val player = parseLine(line)

            if (player != null) {
                // строка валидная, значит добавляем игрока в список
                players.add(player)
            } else {
                // не разобрали строку, считаем её невалидной
                skipped ++
            }
        }

        // возвращаем результат
        return ParseResult(players = players, skippedCount = skipped)
    }

    private fun parseLine(line: String): Player? {
        // разделяем по ';' и убираем пробелы it.trim() у каждой ячейки
        val cells = line.split(DELIMITER).map { it.trim() }

        // если ячеек меньше 12, значит строка испорчена
        if (cells.size < COLUMNS) return null

        // текстовые поля, индексы соответствуют порядку колонок в табличном файле
        val name     = cells[0]
        val teamName = cells[1]
        val teamCity = cells[2]
        val country  = cells[4]

        // имя, команда, город и страна не должны быть пустыми
        if (name.isEmpty() || teamName.isEmpty() || teamCity.isEmpty() || country.isEmpty()) return null

        // позицию распознаём через Position (регистр и пробелы нормализуются там)
        val position = Position.fromCsvTokenOrNull(cells[3]) ?: return null

        // агентство => пустая строка это норм
        val agency = cells[5].ifEmpty { null }

        // числовые поля: пусто/не число => пропустить строку
        val transfer    = cells[6].toLongOrNull() ?: return null
        val matches     = cells[7].toIntOrNull()  ?: return null
        val goals       = cells[8].toIntOrNull()  ?: return null
        val assists     = cells[9].toIntOrNull()  ?: return null
        val yellowCards = cells[10].toIntOrNull() ?: return null
        val redCards    = cells[11].toIntOrNull() ?: return null

        // собираем Player
        return Player(
            name = name,
            team = Team(name = teamName, city = teamCity),
            position = position,
            country = country,
            agency = agency,
            transfer = transfer,
            matches = matches,
            goals = goals,
            assists = assists,
            yellowCards = yellowCards,
            redCards = redCards
        )
    }
}