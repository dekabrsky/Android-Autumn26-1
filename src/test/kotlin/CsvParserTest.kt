import model.Position
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CsvParserTest {

    @Test
    fun `empty agency is mapped to null`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            John Doe;Team A;City A;MIDFIELD;Russia;;1000000;10;5;2;1;0
        """.trimIndent()

        val result = CsvParser.parser(csv)

        assertEquals(1, result.players.size)
        assertNull(result.players[0].agency)
        assertEquals(0, result.skippedCount)
    }

    @Test
    fun `broken rows are skipped and counted`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            John Doe;Team A;City A;MIDFIELD;Russia;Agency A;1000000;10;5;2;1;0
            Broken row
            Jane Doe;Team B;City B;DEFENDER;Brazil;Agency B;2000000;15;7;3;2;0
        """.trimIndent()

        val result = CsvParser.parser(csv)

        assertEquals(2, result.players.size)
        assertEquals(1, result.skippedCount)
    }

    @Test
    fun `position is recognized regardless of case`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            Player 1;Team A;City A;MIDFIELD;Russia;Agency;1000000;10;5;2;1;0
            Player 2;Team B;City B;defender;Brazil;Agency;2000000;10;5;2;1;0
            Player 3;Team C;City C;FoRwArD;France;Agency;3000000;10;5;2;1;0
        """.trimIndent()

        val result = CsvParser.parser(csv)

        assertEquals(3, result.players.size)
        assertEquals(Position.MIDFIELD, result.players[0].position)
        assertEquals(Position.DEFENDER, result.players[1].position)
        assertEquals(Position.FORWARD, result.players[2].position)
        assertEquals(0, result.skippedCount)
    }
}