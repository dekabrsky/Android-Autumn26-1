import model.Position
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CsvParserTest {

    @Test
    fun `empty agency is converted to null`() {
        val line =
            "  Alex  ;  Team One  ;  City One  ;  FoRwArD  ;  Germany  ;   ; 100 ; 10 ; 5 ; 2 ; 1 ; 0"

        val player = CsvParser.parseLine(line)

        assertNotNull(player)
        assertEquals("Alex", player.name)
        assertEquals("Team One", player.team.name)
        assertEquals("City One", player.team.city)
        assertEquals("Germany", player.country)
        assertEquals(Position.FORWARD, player.position)
        assertNull(player.agency)
    }

    @Test
    fun `position parser ignores case and spaces`() {
        assertEquals(
            Position.FORWARD,
            Position.fromCsvTokenOrNull(" forward ")
        )

        assertEquals(
            Position.MIDFIELD,
            Position.fromCsvTokenOrNull("MIDFIELD")
        )

        assertEquals(
            Position.DEFENDER,
            Position.fromCsvTokenOrNull(" DeFeNdEr ")
        )

        assertEquals(
            Position.GOALKEEPER,
            Position.fromCsvTokenOrNull("goalkeeper")
        )
    }

    @Test
    fun `invalid rows are skipped and counted`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            Valid Player;Alpha;Moscow;DEFENDER;Germany;Agency A;100;10;5;2;1;0
            Broken Number;Beta;Berlin;FORWARD;Brazil;Agency B;abc;10;5;2;1;0
            ;Gamma;Paris;MIDFIELD;France;Agency C;200;10;3;1;0;0
        """.trimIndent()

        val result = CsvParser.parse(
            csv.byteInputStream()
        )

        assertEquals(1, result.players.size)
        assertEquals(2, result.skippedCount)
        assertEquals("Valid Player", result.players.first().name)
    }
}