import model.Position
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

class CsvParserTest {

    private val header =
        "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards"

    @Test
    fun `empty agency field maps to null`() {
        val line = "John Doe;Team A;City A;FORWARD;Brazil;;1000000;10;5;3;1;0"
        val player = CsvParser.parsePlayer(line)

        assertNotNull(player)
        assertNull(player.agency)
    }

    @Test
    fun `non-empty agency field is preserved`() {
        val line = "John Doe;Team A;City A;FORWARD;Brazil;Some Agency;1000000;10;5;3;1;0"
        val player = CsvParser.parsePlayer(line)

        assertNotNull(player)
        assertEquals("Some Agency", player.agency)
    }

    @Test
    fun `line with wrong column count is rejected`() {
        val tooFewColumns = "John Doe;Team A;City A;FORWARD;Brazil"
        assertNull(CsvParser.parsePlayer(tooFewColumns))
    }

    @Test
    fun `line with non-numeric transfer cost is rejected`() {
        val badLine = "John Doe;Team A;City A;FORWARD;Brazil;;not_a_number;10;5;3;1;0"
        assertNull(CsvParser.parsePlayer(badLine))
    }

    @Test
    fun `line with empty required field is rejected`() {
        val emptyTeamName = "John Doe;;City A;FORWARD;Brazil;;1000000;10;5;3;1;0"
        assertNull(CsvParser.parsePlayer(emptyTeamName))
    }

    @Test
    fun `parse counts skipped malformed lines separately from valid ones`() {
        val lines = sequenceOf(
            header,
            "John Doe;Team A;City A;FORWARD;Brazil;;1000000;10;5;3;1;0",
            "Bad Row;;City B;MIDFIELD;Germany;;500000;5;1;1;0;0",
            "Jane Roe;Team A;City A;DEFENDER;France;Agency X;2000000;20;2;4;2;1"
        )

        val result = CsvParser.parse(lines)

        assertEquals(2, result.players.size)
        assertEquals(1, result.skippedCount)
    }

    @Test
    fun `position is recognized regardless of case`() {
        val lower = CsvParser.parsePlayer("A;T;C;forward;Brazil;;100;1;1;1;0;0")
        val upper = CsvParser.parsePlayer("A;T;C;FORWARD;Brazil;;100;1;1;1;0;0")
        val mixed = CsvParser.parsePlayer("A;T;C;FoRwArD;Brazil;;100;1;1;1;0;0")

        assertNotNull(lower)
        assertNotNull(upper)
        assertNotNull(mixed)

        assertEquals(Position.FORWARD, lower.position)
        assertEquals(Position.FORWARD, upper.position)
        assertEquals(Position.FORWARD, mixed.position)
    }

    @Test
    fun `unknown position token is rejected`() {
        val line = "A;T;C;STRIKER;Brazil;;100;1;1;1;0;0"
        assertNull(CsvParser.parsePlayer(line))
    }

    @Test
    fun `players from the same team share a single Team instance with filled roster`() {
        val lines = sequenceOf(
            header,
            "A;Team X;City X;FORWARD;Brazil;;100;1;1;1;0;0",
            "B;Team X;City X;DEFENDER;Brazil;;200;2;2;2;1;1"
        )

        val result = CsvParser.parse(lines)

        assertEquals(2, result.players.size)
        assertSame(result.players[0].team, result.players[1].team)
        assertEquals(2, result.players[0].team.players.size)
    }
}