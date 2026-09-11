import model.Position
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CsvParserTest {
    @Test
    fun `empty agency keeps player and maps agency to null`() {
        val result = CsvParser.parse(
            """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            Alex Test;Test FC;Test City;FORWARD;Germany;;1000;10;3;4;1;0
            """.trimIndent(),
        )

        assertEquals(1, result.players.size)
        assertEquals(0, result.skippedCount)
        assertNull(result.players.single().agency)
    }

    @Test
    fun `position parser ignores case and surrounding spaces`() {
        listOf("DEFENDER", "defender", "Defender", "  dEfEnDeR  ").forEach { token ->
            assertEquals(Position.DEFENDER, Position.fromCsvTokenOrNull(token))
        }
    }

    @Test
    fun `invalid numeric field skips row`() {
        val result = CsvParser.parse(
            """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            Alex Test;Test FC;Test City;FORWARD;Germany;Agency;bad;10;3;4;1;0
            """.trimIndent(),
        )

        assertEquals(emptyList(), result.players)
        assertEquals(1, result.skippedCount)
    }

    @Test
    fun `empty required text field skips row`() {
        val result = CsvParser.parse(
            """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            Alex Test;;Test City;FORWARD;Germany;Agency;1000;10;3;4;1;0
            """.trimIndent(),
        )

        assertEquals(emptyList(), result.players)
        assertEquals(1, result.skippedCount)
    }

    @Test
    fun `several broken rows produce correct skipped count`() {
        val result = CsvParser.parse(
            """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            Alex Test;Test FC;Test City;FORWARD;Germany;Agency;1000;10;3;4;1;0
            ;Test FC;Test City;FORWARD;Germany;Agency;1000;10;3;4;1;0
            Bob Test;Test FC;Test City;UNKNOWN;Germany;Agency;1000;10;3;4;1;0
            Carl Test;Test FC;Test City;FORWARD;Germany;Agency;1000;ten;3;4;1;0
            """.trimIndent(),
        )

        assertEquals(1, result.players.size)
        assertEquals(3, result.skippedCount)
    }
}
