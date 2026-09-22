import model.Position
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CsvParserTest {

    @Test
    fun `empty agency becomes null`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            John Doe;Team A;City A;FORWARD;Germany;;100;10;5;3;1;0
        """.trimIndent()

        val result = CsvParser.parseLines(csv.lineSequence())

        assertEquals(1, result.players.size)
        assertNull(result.players.single().agency)
        assertEquals(0, result.skippedCount)
    }

    @Test
    fun `position is recognized ignoring case and spaces`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            John Doe;Team A;City A;   gOaLkEePeR  ;Germany;Agency;100;10;5;3;1;0
        """.trimIndent()

        val result = CsvParser.parseLines(csv.lineSequence())

        assertEquals(Position.GOALKEEPER, result.players.single().position)
    }

    @Test
    fun `broken numeric row is skipped and counted`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            Good Player;Team A;City A;FORWARD;Germany;Agency;100;10;5;3;1;0
            Broken Player;Team A;City A;FORWARD;Germany;Agency;not-a-number;10;5;3;1;0
        """.trimIndent()

        val result = CsvParser.parseLines(csv.lineSequence())

        assertEquals(1, result.players.size)
        assertEquals(1, result.skippedCount)
    }

    @Test
    fun `empty required text field is skipped`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            ;Team A;City A;FORWARD;Germany;Agency;100;10;5;3;1;0
        """.trimIndent()

        val result = CsvParser.parseLines(csv.lineSequence())

        assertEquals(0, result.players.size)
        assertEquals(1, result.skippedCount)
    }
}
