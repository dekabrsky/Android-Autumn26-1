import model.Position
import model.Team
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CsvParserTest {

    private val header =
        "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards"

    private val validLine = "Iva Streich;Nevada whales;South Carolina;MIDFIELD;Colombia;D'Amore LLC;75012006;22;19;6;3;7"

    @Test
    fun `maps valid line to player`() {
        val player = assertNotNull(CsvParser.parseLineOrNull(validLine))
        assertEquals("Iva Streich", player.name)
        assertEquals(Team("Nevada whales", "South Carolina"), player.team)
        assertEquals(Position.MIDFIELD, player.position)
        assertEquals("Colombia", player.nationality)
        assertEquals("D'Amore LLC", player.agency)
        assertEquals(75_012_006L, player.transferCost)
        assertEquals(22, player.participations)
        assertEquals(19, player.goals)
        assertEquals(6, player.assists)
        assertEquals(3, player.yellowCards)
        assertEquals(7, player.redCards)
    }

    @Test
    fun `empty agency becomes null and player is kept`() {
        val line = "Ms. Adolph Hartmann;North Carolina dolphins;Tennessee;FORWARD;Croatia;;52944545;27;10;5;5;8"
        val player = assertNotNull(CsvParser.parseLineOrNull(line))
        assertNull(player.agency)
    }

    @Test
    fun `position is recognized in any case with spaces`() {
        val line = "A;T;C;  goalKeeper ;Brazil;X;100;1;2;3;4;5"
        assertEquals(Position.GOALKEEPER, CsvParser.parseLineOrNull(line)?.position)
    }

    @Test
    fun `text fields are trimmed`() {
        val line = "  Mrs. Adan Swift ; Utah nemesis ; New York ;DEFENDER; Ghana ; Price-Hirthe ;100;1;2;3;4;5"
        val player = assertNotNull(CsvParser.parseLineOrNull(line))
        assertEquals("Mrs. Adan Swift", player.name)
        assertEquals(Team("Utah nemesis", "New York"), player.team)
        assertEquals("Ghana", player.nationality)
        assertEquals("Price-Hirthe", player.agency)
    }

    @Test
    fun `skips rows with empty or non-numeric numbers`() {
        assertNull(CsvParser.parseLineOrNull("A;T;C;FORWARD;Brazil;X;;1;2;3;4;5"), "empty cost")
        assertNull(CsvParser.parseLineOrNull("A;T;C;FORWARD;Brazil;X;100;;2;3;4;5"), "empty participations")
        assertNull(CsvParser.parseLineOrNull("A;T;C;FORWARD;Brazil;X;100;1;;3;4;5"), "empty goals")
        assertNull(CsvParser.parseLineOrNull("A;T;C;FORWARD;Brazil;X;100;1;2;;4;5"), "empty assists")
        assertNull(CsvParser.parseLineOrNull("A;T;C;FORWARD;Brazil;X;100;1;2;3;;5"), "empty yellow cards")
        assertNull(CsvParser.parseLineOrNull("A;T;C;FORWARD;Brazil;X;100;1;2;3;4;"), "empty red cards")
        assertNull(CsvParser.parseLineOrNull("A;T;C;FORWARD;Brazil;X;dear;1;2;3;4;5"), "non-numeric cost")
        assertNull(CsvParser.parseLineOrNull("A;T;C;FORWARD;Brazil;X;100;1;many;3;4;5"), "non-numeric goals")
    }

    @Test
    fun `skips rows with empty required text fields`() {
        assertNull(CsvParser.parseLineOrNull(";T;C;FORWARD;Brazil;X;100;1;2;3;4;5"), "empty name")
        assertNull(CsvParser.parseLineOrNull("A;;C;FORWARD;Brazil;X;100;1;2;3;4;5"), "empty team")
        assertNull(CsvParser.parseLineOrNull("A;T;;FORWARD;Brazil;X;100;1;2;3;4;5"), "empty city")
        assertNull(CsvParser.parseLineOrNull("A;T;C;;Brazil;X;100;1;2;3;4;5"), "empty position")
        assertNull(CsvParser.parseLineOrNull("A;T;C;FORWARD;;X;100;1;2;3;4;5"), "empty nationality")
        assertNull(CsvParser.parseLineOrNull("A;T;C;STRIKER;Brazil;X;100;1;2;3;4;5"), "unknown position")
    }

    @Test
    fun `skips rows with wrong number of columns`() {
        assertNull(CsvParser.parseLineOrNull("A;T;C;FORWARD"))
        assertNull(CsvParser.parseLineOrNull("$validLine;extra"))
    }

    @Test
    fun `counts skipped rows and keeps the good ones`() {
        val lines = listOf(
            header,
            validLine,
            "Broken;T;C;FORWARD;Brazil;X;;1;2;3;4;5",
            "NoAgency;T;C;DEFENDER;Brazil;;100;1;2;3;4;5",
            "NoTeam;;C;FORWARD;Brazil;X;100;1;2;3;4;5",
            "",
        )
        val result = CsvParser.parse(lines)
        assertEquals(2, result.players.size)
        assertEquals(2, result.skippedCount)
        assertEquals(listOf("Iva Streich", "NoAgency"), result.players.map { it.name })
    }

    @Test
    fun `parses clean dataset without skips`() {
        val result = CsvParser.parseResource("fakePlayers.csv")
        assertEquals(250, result.players.size)
        assertEquals(0, result.skippedCount)
    }

    @Test
    fun `parses dirty dataset without crashing`() {
        val result = CsvParser.parseResource("fakePlayersDirty.csv")
        assertEquals(250, result.players.size + result.skippedCount)
        assertEquals(true, result.skippedCount > 0)
    }
}
