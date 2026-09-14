import model.Position
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals

class CsvParserTest {

    private val header =
        "Name;Team;City;Position;country;Agency;" +
                "Transfer cost;appearances;Goals;Assists;" +
                "Yellow cards;Red cards"

    @Test
    fun `empty agency becomes null`() {
        val row =
            "Alice;Team;City;FORWARD;Germany;;" +
                    "50000000;25;10;4;1;0"

        val result = CsvParser.parse(sequenceOf(header, row))

        assertEquals(1, result.players.size)
        assertEquals(null, result.players.single().agency)
        assertEquals(0, result.skippedCount)
    }

    @Test
    fun `position ignores case and spaces`() {
        val row =
            "Alice;Team;City;  dEfEnDeR  ;Germany;Agency;" +
                    "50000000;25;10;4;1;0"

        val result = CsvParser.parse(sequenceOf(header, row))

        assertEquals(
            Position.DEFENDER,
            result.players.single().position
        )
    }

    @Test
    fun `broken rows are skipped and counted`() {
        val valid =
            "Alice;Team;City;FORWARD;Germany;Agency;" +
                    "50000000;25;10;4;1;0"

        val invalidNumber =
            "Bob;Team;City;DEFENDER;Brazil;Agency;" +
                    "wrong;25;10;4;1;0"

        val emptyName =
            ";Team;City;DEFENDER;Brazil;Agency;" +
                    "10000000;25;10;4;1;0"

        val result = CsvParser.parse(
            sequenceOf(header, valid, invalidNumber, emptyName)
        )

        assertEquals(1, result.players.size)
        assertEquals(2, result.skippedCount)
    }
}