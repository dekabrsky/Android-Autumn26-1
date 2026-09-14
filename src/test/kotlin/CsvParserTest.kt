import model.Position
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CsvParserTest {

    private val header =
        "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards"

    private val validLine =
        "Iva Streich;Nevada whales;South Carolina;MIDFIELD;Colombia;D'Amore LLC;75012006;22;19;6;3;7"

    @Test
    fun `valid line is mapped into a player`() {
        val player = assertNotNull(CsvParser.parseLine(validLine))

        assertEquals("Iva Streich", player.name)
        assertEquals("Nevada whales", player.team.name)
        assertEquals("South Carolina", player.team.city)
        assertEquals(Position.MIDFIELD, player.position)
        assertEquals("Colombia", player.country)
        assertEquals("D'Amore LLC", player.agency)
        assertEquals(75012006L, player.transferCost)
        assertEquals(22, player.participations)
        assertEquals(19, player.goals)
        assertEquals(6, player.assists)
        assertEquals(3, player.yellowCards)
        assertEquals(7, player.redCards)
    }

    @Test
    fun `empty agency becomes null and the player stays in the selection`() {
        val player = assertNotNull(
            CsvParser.parseLine("Ms. Adolph Hartmann;North Carolina dolphins;Tennessee;FORWARD;Croatia;;52944545;27;10;5;5;8")
        )

        assertNull(player.agency)
        assertEquals("Ms. Adolph Hartmann", player.name)
    }

    @Test
    fun `extra spaces in text cells are trimmed`() {
        val player = assertNotNull(
            CsvParser.parseLine("   Iva Streich  ;  Nevada whales ; South Carolina  ;  midfield ; Colombia ;  D'Amore LLC ;75012006;22;19;6;3;7")
        )

        assertEquals("Iva Streich", player.name)
        assertEquals("Nevada whales", player.team.name)
        assertEquals("South Carolina", player.team.city)
        assertEquals("Colombia", player.country)
        assertEquals("D'Amore LLC", player.agency)
        assertEquals(Position.MIDFIELD, player.position)
    }

    @Test
    fun `position in any case is recognized`() {
        val lowercase = assertNotNull(
            CsvParser.parseLine("Terry Hegmann;Wisconsin prophets;Vermont;defender;Germany;Graham-Powlowski;7294362;12;11;28;1;8")
        )
        assertEquals(Position.DEFENDER, lowercase.position)
    }

    @Test
    fun `rows with empty required text cells are skipped`() {
        val withoutName = validLine.replaceFirst("Iva Streich", "")
        val withoutTeam = validLine.replaceFirst("Nevada whales", "")
        val withoutCity = validLine.replaceFirst("South Carolina", "")
        val withoutCountry = validLine.replaceFirst("Colombia", "")
        val withoutPosition = validLine.replaceFirst("MIDFIELD", "")

        assertNull(CsvParser.parseLine(withoutName))
        assertNull(CsvParser.parseLine(withoutTeam))
        assertNull(CsvParser.parseLine(withoutCity))
        assertNull(CsvParser.parseLine(withoutCountry))
        assertNull(CsvParser.parseLine(withoutPosition))
    }

    @Test
    fun `rows with empty or non numeric numbers are skipped`() {
        assertNull(CsvParser.parseLine(validLine.replaceFirst("75012006", "")))
        assertNull(CsvParser.parseLine(validLine.replaceFirst("75012006", "дорого")))
        assertNull(CsvParser.parseLine(validLine.replaceFirst(";19;", ";;")))
        assertNull(CsvParser.parseLine(validLine.replaceFirst(";19;", ";много;")))
        assertNull(CsvParser.parseLine(validLine.replaceFirst(";22;", ";;")))
    }

    @Test
    fun `unknown position is skipped`() {
        assertNull(CsvParser.parseLine(validLine.replaceFirst("MIDFIELD", "striker")))
    }

    @Test
    fun `row with a wrong number of columns is skipped`() {
        assertNull(CsvParser.parseLine("Iva Streich;Nevada whales;South Carolina"))
        assertNull(CsvParser.parseLine("$validLine;extra"))
    }

    @Test
    fun `parser counts skipped rows and ignores header and blank lines`() {
        val lines = listOf(
            header,
            validLine,
            "",
            "   ",
            validLine.replaceFirst("75012006", ""),
            validLine.replaceFirst("MIDFIELD", ""),
            "broken line without delimiters",
        )

        val result = CsvParser.parse(lines)

        assertEquals(1, result.players.size)
        assertEquals(3, result.skippedCount)
        assertEquals(4, result.totalRows)
    }

    @Test
    fun `clean dataset is parsed without losses`() {
        val result = CsvParser.parseResource("fakePlayers.csv")

        assertEquals(250, result.players.size)
        assertEquals(0, result.skippedCount)
        assertTrue(result.players.all { it.name.isNotBlank() })
    }

    @Test
    fun `dirty dataset survives broken rows`() {
        val result = CsvParser.parseResource("fakePlayersDirty.csv")

        assertEquals(220, result.players.size)
        assertEquals(30, result.skippedCount)
        assertEquals(250, result.totalRows)
        assertEquals(40, result.players.count { it.agency == null })
    }
}
