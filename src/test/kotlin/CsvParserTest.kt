import model.Position
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CsvParserTest {

    private val HEADER =
        "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards"

    private fun parse(vararg lines: String) = CsvParser.parse(lines.asSequence())

    @Test
    fun `header is skipped`() {
        val r = parse(
            HEADER,
            "John;Team A;City A;FORWARD;Germany;AgencyX;1000000;10;5;3;1;0"
        )
        assertEquals(1, r.players.size)
        assertEquals(0, r.skippedCount)
    }

    @Test
    fun `empty agency becomes null`() {
        val r = parse(
            HEADER,
            "John;Team A;City A;FORWARD;Germany;;1000000;10;5;3;1;0"
        )
        assertNull(r.players.single().agency)
    }

    @Test
    fun `position is case-insensitive and trims spaces`() {
        assertEquals(Position.FORWARD,    Position.fromCsvTokenOrNull("forward"))
        assertEquals(Position.FORWARD,    Position.fromCsvTokenOrNull(" FORWARD "))
        assertEquals(Position.GOALKEEPER, Position.fromCsvTokenOrNull("Goalkeeper"))
        assertEquals(Position.MIDFIELD,   Position.fromCsvTokenOrNull("midfield"))
        assertNull(Position.fromCsvTokenOrNull(""))
        assertNull(Position.fromCsvTokenOrNull("coach"))
    }

    @Test
    fun `broken numeric line is skipped and counted`() {
        val r = parse(
            HEADER,
            "John;Team A;City A;FORWARD;Germany;AgencyX;1000000;10;5;3;1;0",
            "Bad;Team A;City A;FORWARD;Germany;AgencyX;NOT_A_NUMBER;10;5;3;1;0",
            "Bad2;Team A;City A;FORWARD;Germany;AgencyX;;10;5;3;1;0",
            "Bad3;Team A;City A;FORWARD;Germany;AgencyX;1000;10;;3;1;0"
        )
        assertEquals(1, r.players.size)
        assertEquals(3, r.skippedCount)
    }

    @Test
    fun `broken text line is skipped`() {
        val r = parse(
            HEADER,
            "Bad;;City A;FORWARD;Germany;AgencyX;1000000;10;5;3;1;0",
            "Bad2;Team A;;FORWARD;Germany;AgencyX;1000000;10;5;3;1;0",
            "Bad3;Team A;City A;;Germany;AgencyX;1000000;10;5;3;1;0"
        )
        assertEquals(0, r.players.size)
        assertEquals(3, r.skippedCount)
    }

    @Test
    fun `name with extra spaces is trimmed`() {
        val r = parse(
            HEADER,
            "  John Doe  ;Team A;City A;FORWARD;Germany;AgencyX;1000000;10;5;3;1;0"
        )
        assertEquals("John Doe", r.players.single().name)
    }
}