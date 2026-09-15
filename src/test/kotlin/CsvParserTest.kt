import model.Position
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CsvParserTest {

    @Test
    fun `empty agency becomes null`() {
        val line =
            "John Doe,United,London,FORWARD,England,,10000000,20,10,5,2,0"

        val player = CsvParser.parseLine(line)

        assertNull(player?.agency)
    }

    @Test
    fun `position ignores case and spaces`() {
        assertEquals(
            Position.DEFENDER,
            Position.fromCsvTokenOrNull("  deFEnder  ")
        )

        assertEquals(
            Position.FORWARD,
            Position.fromCsvTokenOrNull(" forward ")
        )
    }

    @Test
    fun `invalid number causes row to be skipped`() {
        val line =
            "John Doe,United,London,FORWARD,England,Agency,broken,20,10,5,2,0"

        assertNull(CsvParser.parseLine(line))
    }

    @Test
    fun `empty required field causes row to be skipped`() {
        val line =
            ",United,London,FORWARD,England,Agency,10000000,20,10,5,2,0"

        assertNull(CsvParser.parseLine(line))
    }

    @Test
    fun `parser counts skipped rows`() {
        val lines = listOf(
            // Заголовок не считается пропущенной строкой
            "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards",

            // Корректная строка
            "John;United;London;FORWARD;England;;10000000;20;10;5;2;0",

            // Некорректная стоимость
            "Bad Cost;United;London;FORWARD;England;Agency;xxx;20;10;5;2;0",

            // Пустое обязательное имя
            ";United;London;DEFENDER;England;Agency;100;20;1;1;0;0"
        )

        val result = CsvParser.parseLines(lines)

        assertEquals(1, result.players.size)
        assertEquals(2, result.skippedCount)
    }
}