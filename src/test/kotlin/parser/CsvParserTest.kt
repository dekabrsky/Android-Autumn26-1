package parser

import model.Position
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CsvParserTest {
    private val header =
        "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards"

    @Test
    fun `empty agency becomes null and text values are trimmed`() {
        val result = CsvParser.parse(
            sequenceOf(
                header,
                "  Alex  ;  Team A ; City ;  fOrWaRd ; Germany ;   ;100;10;3;2;1;0",
            ),
        )

        assertEquals(1, result.players.size)
        assertEquals(0, result.skippedCount)
        with(result.players.single()) {
            assertEquals("Alex", name)
            assertEquals("Team A", team.name)
            assertEquals(Position.FORWARD, position)
            assertNull(agency)
        }
    }

    @Test
    fun `broken rows are skipped and counted`() {
        val result = CsvParser.parse(
            sequenceOf(
                header,
                "Valid;Team;City;DEFENDER;Germany;Agency;100;10;3;2;1;0",
                ";Team;City;DEFENDER;Germany;Agency;100;10;3;2;1;0",
                "Alex;Team;City;UNKNOWN;Germany;Agency;100;10;3;2;1;0",
                "Alex;Team;City;DEFENDER;Germany;Agency;not-a-number;10;3;2;1;0",
                "Alex;Team;City;DEFENDER;Germany;Agency;100;10;;2;1;0",
                "too;few;columns",
            ),
        )

        assertEquals(1, result.players.size)
        assertEquals(5, result.skippedCount)
    }

    @Test
    fun `position parser ignores case and surrounding spaces`() {
        assertEquals(Position.GOALKEEPER, Position.fromCsvTokenOrNull("  gOaLkEePeR "))
        assertNull(Position.fromCsvTokenOrNull("coach"))
        assertNull(Position.fromCsvTokenOrNull("   "))
    }
}
