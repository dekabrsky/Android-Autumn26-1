import model.Position
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CsvParserTest {

    @Test
    fun `empty agency becomes null and text fields are trimmed`() {
        val line =
            "  Ivan  ;  Team A  ;  City A  ;  fOrWaRd  ;  Germany  ;   ; 100 ; 10 ; 5 ; 2 ; 1 ; 0"

        val player = CsvParser.parseLine(line)

        assertNotNull(player)

        assertEquals("Ivan", player.name)
        assertEquals("Team A", player.team.name)
        assertEquals("City A", player.team.city)
        assertEquals("Germany", player.country)

        assertEquals(Position.FORWARD, player.position)

        assertNull(player.agency)
    }

    @Test
    fun `position is recognized regardless of case and spaces`() {
        assertEquals(
            Position.FORWARD,
            Position.fromCsvTokenOrNull("  forward  ")
        )

        assertEquals(
            Position.MIDFIELD,
            Position.fromCsvTokenOrNull("MidField")
        )

        assertEquals(
            Position.DEFENDER,
            Position.fromCsvTokenOrNull("  DeFeNdEr ")
        )

        assertEquals(
            Position.GOALKEEPER,
            Position.fromCsvTokenOrNull("goalkeeper")
        )
    }

    @Test
    fun `broken rows are skipped and counted`() {

        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            Good Player;Team A;City A;DEFENDER;Germany;Agency A;100;10;5;2;1;0
            Bad Number;Team B;City B;FORWARD;Brazil;Agency B;not-a-number;10;5;2;1;0
            ;Team C;City C;MIDFIELD;France;Agency C;200;10;3;1;0;0
        """.trimIndent()

        val result = CsvParser.parse(
            csv.byteInputStream()
        )

        assertEquals(1, result.players.size)
        assertEquals(2, result.skippedCount)

        assertEquals(
            "Good Player",
            result.players.first().name
        )
    }
}