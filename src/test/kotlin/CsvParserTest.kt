import model.Position
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CsvParserTest {

    private val header = "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards"

    @Test
    fun `empty agency becomes null and player is kept`() {
        val csv = listOf(
            header,
            "John Doe;Nevada whales;South Carolina;FORWARD;Brazil;;1000000;10;5;3;1;0"
        )

        val result = CsvParser.parse(csv)

        assertEquals(0, result.skippedCount)
        assertEquals(1, result.players.size)
        assertNull(result.players.first().agency)
    }

    @Test
    fun `non-empty agency is trimmed and kept`() {
        val csv = listOf(
            header,
            "John Doe;Nevada whales;South Carolina;FORWARD;Brazil;  D'Amore LLC ;1000000;10;5;3;1;0"
        )

        val result = CsvParser.parse(csv)

        assertEquals("D'Amore LLC", result.players.first().agency)
    }

    @Test
    fun `position is recognized regardless of case and surrounding spaces`() {
        val csv = listOf(
            header,
            "John Doe;Nevada whales;South Carolina; defender ;Brazil;Agency;1000000;10;5;3;1;0",
            "Jane Roe;Nevada whales;South Carolina;GOALKEEPER;Brazil;Agency;1000000;10;5;3;1;0"
        )

        val result = CsvParser.parse(csv)

        assertEquals(0, result.skippedCount)
        assertEquals(Position.DEFENDER, result.players[0].position)
        assertEquals(Position.GOALKEEPER, result.players[1].position)
    }

    @Test
    fun `row with unrecognized position is skipped`() {
        val csv = listOf(
            header,
            "John Doe;Nevada whales;South Carolina;STRIKER;Brazil;Agency;1000000;10;5;3;1;0"
        )

        val result = CsvParser.parse(csv)

        assertEquals(1, result.skippedCount)
        assertEquals(0, result.players.size)
    }

    @Test
    fun `row with non-numeric transfer cost is skipped`() {
        val csv = listOf(
            header,
            "John Doe;Nevada whales;South Carolina;FORWARD;Brazil;Agency;not-a-number;10;5;3;1;0"
        )

        val result = CsvParser.parse(csv)

        assertEquals(1, result.skippedCount)
        assertEquals(0, result.players.size)
    }

    @Test
    fun `row with empty required text field is skipped`() {
        val csv = listOf(
            header,
            ";Nevada whales;South Carolina;FORWARD;Brazil;Agency;1000000;10;5;3;1;0",
            "John Doe;Nevada whales;South Carolina;FORWARD;;Agency;1000000;10;5;3;1;0"
        )

        val result = CsvParser.parse(csv)

        assertEquals(2, result.skippedCount)
        assertEquals(0, result.players.size)
    }

    @Test
    fun `valid and invalid rows are counted correctly together`() {
        val csv = listOf(
            header,
            "John Doe;Nevada whales;South Carolina;FORWARD;Brazil;Agency;1000000;10;5;3;1;0",
            "Bad Row;Nevada whales;South Carolina;UNKNOWN;Brazil;Agency;1000000;10;5;3;1;0",
            "Jane Roe;Nevada whales;South Carolina;DEFENDER;Brazil;;2000000;5;1;1;0;0"
        )

        val result = CsvParser.parse(csv)

        assertEquals(2, result.players.size)
        assertEquals(1, result.skippedCount)
    }

    @Test
    fun `text fields are trimmed`() {
        val csv = listOf(
            header,
            "  John Doe  ;  Nevada whales  ;  South Carolina  ;FORWARD;  Brazil  ;Agency;1000000;10;5;3;1;0"
        )

        val result = CsvParser.parse(csv)
        val player = result.players.first()

        assertEquals("John Doe", player.name)
        assertEquals("Nevada whales", player.team.name)
        assertEquals("South Carolina", player.team.city)
        assertEquals("Brazil", player.country)
    }
}
