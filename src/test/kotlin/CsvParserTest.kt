import model.Position
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CsvParserTest {
    @Test
    fun `empty agency becomes null`() {
        val player = CsvParser.parseLine(
            "Ivan Ivanov;Team A;Moscow;FORWARD;Russia;;1000000;10;5;2;1;0"
        )

        assertNull(player?.agency)
    }

    @Test
    fun `position ignores case and spaces`() {
        assertEquals(Position.DEFENDER, Position.fromCsvTokenOrNull("  defender  "))
        assertEquals(Position.FORWARD, Position.fromCsvTokenOrNull("forward"))
        assertEquals(Position.MIDFIELD, Position.fromCsvTokenOrNull(" MidField "))
    }

    @Test
    fun `broken rows are skipped and counted`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            Good Player;Team A;Moscow;FORWARD;Russia;Agency;100;10;5;2;1;0
            Bad Cost;Team B;Kazan;DEFENDER;Russia;Agency;abc;10;5;2;1;0
            ;Team C;Perm;MIDFIELD;Russia;Agency;200;10;5;2;1;0
        """.trimIndent()

        val result = CsvParser.parse(csv.byteInputStream())

        assertEquals(1, result.players.size)
        assertEquals(2, result.skippedCount)
    }
}
