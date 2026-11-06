package task

import model.Player
import parser.CsvParser

/**
 * Задание 10: первый игрок дороже 50 000 000, с >= 25 матчами и без карточек.
 * Файл читается построчно (useLines + Sequence); firstOrNull останавливает чтение на первом совпадении.
 */
fun findFirstCleanStar(resourceName: String): Player? =
    CsvParser.openResource(resourceName).bufferedReader().useLines { lines ->
        lines.drop(1)
            .filter { it.isNotBlank() }
            .mapNotNull { CsvParser.parseLine(it) }
            .firstOrNull { it.transferCost > 50_000_000 && it.appearances >= 25 && it.yellowCards == 0 && it.redCards == 0 }
    }
