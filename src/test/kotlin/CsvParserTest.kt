import model.Position
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CsvParserTest {

    @Test
    fun `empty agency becomes null`() {
        val line =
            "Test Player;Team A;Moscow;FORWARD;Russia;;1000000;10;2;3;1;0"

        val player = CsvParser.parseLine(line)

        assertEquals("Test Player", player?.name)
        assertNull(player?.agency)
    }

    @Test
    fun `position ignores case and spaces`() {
        val line =
            "Test Player;Team A;Moscow;   dEfEnDeR   ;Russia;Agency;1000000;10;2;3;1;0"

        val player = CsvParser.parseLine(line)

        assertEquals(Position.DEFENDER, player?.position)
    }

    @Test
    fun `broken rows are skipped and counted`() {
        val csv = """
            name;team;city;position;country;agency;cost;matches;goals;assists;yellow;red
            Good Player;Team A;Moscow;FORWARD;Russia;Agency;1000000;10;2;3;1;0
            Broken Cost;Team A;Moscow;FORWARD;Russia;Agency;abc;10;2;3;1;0
            ;Team A;Moscow;FORWARD;Russia;Agency;1000000;10;2;3;1;0
        """.trimIndent()

        val result = CsvParser.parse(csv.byteInputStream())

        assertEquals(1, result.players.size)
        assertEquals(2, result.skippedCount)
    }
}