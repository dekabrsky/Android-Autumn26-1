import model.Position
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CsvParserTest {
    private val header = "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards"

    @Test
    fun `empty agency maps to null and player is kept`() {
        val line = "Ivan Ivanov;Nevada whales;South Carolina;DEFENDER;Brazil;;1000000;10;1;2;3;0"
        val player = CsvParser.parseLine(line)
        assertNull(player?.agency)
        assertEquals("Ivan Ivanov", player?.name)
    }

    @Test
    fun `position is recognized regardless of case and surrounding spaces`() {
        assertEquals(Position.DEFENDER, Position.fromCsvTokenOrNull("defender"))
        assertEquals(Position.DEFENDER, Position.fromCsvTokenOrNull("  DEFENDER  "))
        assertEquals(Position.GOALKEEPER, Position.fromCsvTokenOrNull("GoalKeeper"))
        assertNull(Position.fromCsvTokenOrNull("striker"))
        assertNull(Position.fromCsvTokenOrNull(""))
    }

    @Test
    fun `text fields are trimmed`() {
        val line = "  Ivan Ivanov ;Nevada whales;South Carolina;DEFENDER;Brazil;Some Agency;1000000;10;1;2;3;0"
        val player = CsvParser.parseLine(line)
        assertEquals("Ivan Ivanov", player?.name)
    }

    @Test
    fun `row with unparsable transfer cost is skipped`() {
        val line = "Ivan Ivanov;Nevada whales;South Carolina;DEFENDER;Brazil;Some Agency;not-a-number;10;1;2;3;0"
        assertNull(CsvParser.parseLine(line))
    }

    @Test
    fun `row with unrecognized position is skipped`() {
        val line = "Ivan Ivanov;Nevada whales;South Carolina;STRIKER;Brazil;Some Agency;1000000;10;1;2;3;0"
        assertNull(CsvParser.parseLine(line))
    }

    @Test
    fun `row with blank required text field is skipped`() {
        val line = ";Nevada whales;South Carolina;DEFENDER;Brazil;Some Agency;1000000;10;1;2;3;0"
        assertNull(CsvParser.parseLine(line))
    }

    @Test
    fun `parse counts skipped rows and keeps valid ones`() {
        val lines = listOf(
            header,
            "Ivan Ivanov;Nevada whales;South Carolina;DEFENDER;Brazil;Some Agency;1000000;10;1;2;3;0",
            "Broken Row;Nevada whales;South Carolina;STRIKER;Brazil;Some Agency;1000000;10;1;2;3;0",
            "Petr Petrov;Nevada whales;South Carolina;FORWARD;Brazil;;2000000;5;3;1;0;0",
        )
        val result = CsvParser.parse(lines)
        assertEquals(2, result.players.size)
        assertEquals(1, result.skippedCount)
    }
}
