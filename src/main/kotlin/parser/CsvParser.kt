package parser

import model.Player
import model.Position
import model.Team

/*
| Что встретилось в ячейке                | Что делать            |
|-----------------------------------------|-----------------------|
| Пустое агентство                        | `null`, игрок остаётся в выборке |
| Позиция в любом регистре, с пробелами   | нормализовать и распознать |
| Лишние пробелы в текстовых полях        | обрезать              |
| Пустая или нечисловая стоимость, голы, передачи, карточки, матчи | пропустить строку |
| Пустые имя, команда, город, страна, позиция | пропустить строку |

Парсер не должен падать на некорректных строках, а по итогам парсинга
должно быть известно, сколько строк пропущено (например, верните
`ParseResult(players, skippedCount)`).
*/
data class ParseResult (
    val players: List<Player>,
    val skippedCount: Int
)

object CsvParser {
    fun parser (csv: String): ParseResult {
        var skippedCount = 0;
        val players = mutableListOf<Player>();
        for (line in csv.lines().drop(1)) {
            val parts = line.split(";");

            if (parts.size < 12) {
                skippedCount++
                continue
            }

            val name = parts[0].trim();
            val teamName = parts[1].trim();
            val teamCity = parts[2].trim();
            val position = Position.fromCsvTokenOrNull(parts[3]);
            val country = parts[4].trim();
            val agency = parts[5].trim().ifBlank { null };
            val transferCost = parts[6].trim().toLongOrNull();
            val participations = parts[7].trim().toIntOrNull();
            val goals = parts[8].trim().toIntOrNull();
            val assists = parts[9].trim().toIntOrNull();
            val yellowCards = parts[10].trim().toIntOrNull();
            val redCards = parts[11].trim().toIntOrNull();

            if (name.isBlank() ||
                teamName.isBlank() ||
                teamCity.isBlank() ||
                position == null ||
                country.isBlank() ||
                transferCost == null ||
                participations == null ||
                goals == null ||
                assists == null ||
                yellowCards == null ||
                redCards == null) {
                skippedCount++;
                continue
            }

            players.add(Player(
                name,
                Team(teamName, teamCity),
                position,
                country,
                agency,
                transferCost,
                participations,
                goals,
                assists,
                yellowCards,
                redCards)
            )
        }
        return ParseResult(players, skippedCount)
    }
}