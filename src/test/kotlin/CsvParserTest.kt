import model.Player
import model.Position
import model.Team
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CsvParserTest {

    private val header =
        "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards"

    private val validLine = "Iva Streich;Nevada whales;South Carolina;MIDFIELD;Colombia;D'Amore LLC;75012006;22;19;6;3;7"

    @Test
    fun `parses valid line into player`() {
        val expected = Player(
            name = "Iva Streich",
            team = Team("Nevada whales", "South Carolina"),
            position = Position.MIDFIELD,
            nationality = "Colombia",
            agency = "D'Amore LLC",
            transferCost = 75012006,
            participations = 22,
            goals = 19,
            assists = 6,
            yellowCards = 3,
            redCards = 7,
        )
        assertEquals(expected, CsvParser.parseLineOrNull(validLine))
    }

    @Test
    fun `empty agency becomes null and player stays`() {
        val player = CsvParser.parseLineOrNull("Micha Koch;New Mexico ogres;Georgia;GOALKEEPER;Croatia;;32216127;15;20;0;7;1")
        assertNotNull(player)
        assertNull(player.agency)
    }

    @Test
    fun `blank agency becomes null`() {
        val player = CsvParser.parseLineOrNull("Micha Koch;New Mexico ogres;Georgia;GOALKEEPER;Croatia;   ;32216127;15;20;0;7;1")
        assertNotNull(player)
        assertNull(player.agency)
    }

    @Test
    fun `recognizes position in any case with spaces`() {
        val player = CsvParser.parseLineOrNull("Micha Koch;New Mexico ogres;Georgia;  defender ;Croatia;;32216127;15;20;0;7;1")
        assertEquals(Position.DEFENDER, player?.position)
    }

    @Test
    fun `trims text and numeric fields`() {
        val player = CsvParser.parseLineOrNull("  Micha Koch ; New Mexico ogres ; Georgia ;GOALKEEPER; Croatia ; Fadel LLC ; 32216127 ; 15;20 ;0;7;1")
        assertNotNull(player)
        assertEquals("Micha Koch", player.name)
        assertEquals(Team("New Mexico ogres", "Georgia"), player.team)
        assertEquals("Croatia", player.nationality)
        assertEquals("Fadel LLC", player.agency)
        assertEquals(32216127, player.transferCost)
        assertEquals(15, player.participations)
    }

    @Test
    fun `skips line with empty or non numeric numbers`() {
        val broken = listOf(
            "A;T;C;FORWARD;Ghana;X;;22;19;6;3;7", // пустая стоимость
            "A;T;C;FORWARD;Ghana;X;abc;22;19;6;3;7", // нечисловая стоимость
            "A;T;C;FORWARD;Ghana;X;100;;19;6;3;7", // пустые матчи
            "A;T;C;FORWARD;Ghana;X;100;22;many;6;3;7", // нечисловые голы
            "A;T;C;FORWARD;Ghana;X;100;22;19;;3;7", // пустые передачи
            "A;T;C;FORWARD;Ghana;X;100;22;19;6;1.5;7", // нецелые жёлтые
            "A;T;C;FORWARD;Ghana;X;100;22;19;6;3;-1", // отрицательные красные
        )
        broken.forEach { assertNull(CsvParser.parseLineOrNull(it), "Строка должна быть пропущена: $it") }
    }

    @Test
    fun `skips line with empty required text fields`() {
        val broken = listOf(
            " ;T;C;FORWARD;Ghana;X;100;22;19;6;3;7", // имя
            "A;;C;FORWARD;Ghana;X;100;22;19;6;3;7", // команда
            "A;T;;FORWARD;Ghana;X;100;22;19;6;3;7", // город
            "A;T;C;;Ghana;X;100;22;19;6;3;7", // позиция
            "A;T;C;FORWARD;;X;100;22;19;6;3;7", // страна
        )
        broken.forEach { assertNull(CsvParser.parseLineOrNull(it), "Строка должна быть пропущена: $it") }
    }

    @Test
    fun `skips line with unknown position or wrong column count`() {
        assertNull(CsvParser.parseLineOrNull("A;T;C;STRIKER;Ghana;X;100;22;19;6;3;7"))
        assertNull(CsvParser.parseLineOrNull("A;T;C;FORWARD;Ghana;X;100;22;19;6;3"))
        assertNull(CsvParser.parseLineOrNull("garbage"))
    }

    @Test
    fun `counts skipped lines and ignores header and blank lines`() {
        val text = listOf(
            header,
            validLine,
            "Bad;T;C;FORWARD;Ghana;X;;22;19;6;3;7",
            "Micha Koch;New Mexico ogres;Georgia;goalkeeper;Croatia;;32216127;15;20;0;7;1",
            ";T;C;FORWARD;Ghana;X;100;22;19;6;3;7",
            "",
        ).joinToString("\r\n")

        val result = CsvParser.parse(text)

        assertEquals(listOf("Iva Streich", "Micha Koch"), result.players.map { it.name })
        assertEquals(2, result.skippedCount)
    }

    @Test
    fun `clean dataset is parsed without skips`() {
        val result = CsvParser.parseResource("fakePlayers.csv")
        assertEquals(250, result.players.size)
        assertEquals(0, result.skippedCount)
    }

    @Test
    fun `dirty dataset is parsed without crash`() {
        val result = CsvParser.parseResource("fakePlayersDirty.csv")
        assertEquals(250, result.players.size + result.skippedCount)
        assertEquals(30, result.skippedCount)
        assertTrue(result.players.any { it.agency == null })
    }
}
