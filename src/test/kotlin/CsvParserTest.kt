import model.Position
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CsvParserTest {
    private val valid = "Ivan Ivanov;Reds;Rome;FORWARD;Spain;Alpha;1000;20;5;3;2;1"

    @Test
    fun `valid line is parsed`() {
        val p = assertNotNull(CsvParser.parseLine(valid))
        assertEquals("Ivan Ivanov", p.name)
        assertEquals("Reds", p.teamName)
        assertEquals("Rome", p.city)
        assertEquals(Position.FORWARD, p.position)
        assertEquals("Alpha", p.agency)
        assertEquals(1000L, p.transferCost)
        assertEquals(20, p.appearances)
        assertEquals(5, p.goals)
        assertEquals(3, p.assists)
        assertEquals(2, p.yellowCards)
        assertEquals(1, p.redCards)
    }

    @Test
    fun `empty agency becomes null and player is kept`() {
        val p = assertNotNull(CsvParser.parseLine("Ivan;Reds;Rome;FORWARD;Spain;;1000;20;5;3;2;1"))
        assertNull(p.agency)
    }

    @Test
    fun `position is recognized in any case with spaces`() {
        for (token in listOf("defender", "DEFENDER", "  Defender  ")) {
            val p = assertNotNull(CsvParser.parseLine("Ivan;Reds;Rome;$token;Spain;Alpha;1000;20;5;3;2;1"))
            assertEquals(Position.DEFENDER, p.position)
        }
    }

    @Test
    fun `extra spaces in text fields are trimmed`() {
        val p = assertNotNull(CsvParser.parseLine("  Ivan Ivanov ; Reds ; Rome ;FORWARD; Spain ; Alpha ;1000;20;5;3;2;1"))
        assertEquals("Ivan Ivanov", p.name)
        assertEquals("Reds", p.teamName)
        assertEquals("Rome", p.city)
        assertEquals("Spain", p.country)
        assertEquals("Alpha", p.agency)
    }

    @Test
    fun `blank agency made of spaces is null`() {
        val p = assertNotNull(CsvParser.parseLine("Ivan;Reds;Rome;FORWARD;Spain;   ;1000;20;5;3;2;1"))
        assertNull(p.agency)
    }

    @Test
    fun `empty or non numeric numeric cells skip the line`() {
        val broken = listOf(
            "Ivan;Reds;Rome;FORWARD;Spain;Alpha;;20;5;3;2;1",     // стоимость
            "Ivan;Reds;Rome;FORWARD;Spain;Alpha;abc;20;5;3;2;1",  // стоимость
            "Ivan;Reds;Rome;FORWARD;Spain;Alpha;1000;;5;3;2;1",   // матчи
            "Ivan;Reds;Rome;FORWARD;Spain;Alpha;1000;20;x;3;2;1", // голы
            "Ivan;Reds;Rome;FORWARD;Spain;Alpha;1000;20;5;;2;1",  // передачи
            "Ivan;Reds;Rome;FORWARD;Spain;Alpha;1000;20;5;3;?;1", // жёлтые
            "Ivan;Reds;Rome;FORWARD;Spain;Alpha;1000;20;5;3;2;",  // красные
        )
        broken.forEach { assertNull(CsvParser.parseLine(it), "must be skipped: $it") }
    }

    @Test
    fun `empty text fields and unknown position skip the line`() {
        val broken = listOf(
            ";Reds;Rome;FORWARD;Spain;Alpha;1000;20;5;3;2;1",
            "Ivan;;Rome;FORWARD;Spain;Alpha;1000;20;5;3;2;1",
            "Ivan;Reds;;FORWARD;Spain;Alpha;1000;20;5;3;2;1",
            "Ivan;Reds;Rome;;Spain;Alpha;1000;20;5;3;2;1",
            "Ivan;Reds;Rome;COACH;Spain;Alpha;1000;20;5;3;2;1",
            "Ivan;Reds;Rome;FORWARD;;Alpha;1000;20;5;3;2;1",
        )
        broken.forEach { assertNull(CsvParser.parseLine(it), "must be skipped: $it") }
    }

    @Test
    fun `wrong number of columns skips the line without exception`() {
        assertNull(CsvParser.parseLine("garbage"))
        assertNull(CsvParser.parseLine(""))
        assertNull(CsvParser.parseLine("$valid;extra"))
    }

    @Test
    fun `parse counts skipped lines and ignores header`() {
        val lines = listOf(
            "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards",
            valid,
            "Ivan;Reds;Rome;FORWARD;Spain;Alpha;;20;5;3;2;1",
            "",
            "garbage",
            "Petr;Reds;Rome;goalkeeper;Spain;;500;1;0;0;0;0",
        )
        val result = CsvParser.parse(lines)
        assertEquals(2, result.players.size)
        assertEquals(2, result.skippedCount) // пустая строка не считается пропущенной
    }

    @Test
    fun `parse without header`() {
        val result = CsvParser.parse(listOf(valid), hasHeader = false)
        assertEquals(1, result.players.size)
        assertEquals(0, result.skippedCount)
    }

    @Test
    fun `position token recognition`() {
        assertEquals(Position.FORWARD, Position.fromCsvTokenOrNull("forward"))
        assertEquals(Position.MIDFIELD, Position.fromCsvTokenOrNull(" MidField "))
        assertEquals(Position.GOALKEEPER, Position.fromCsvTokenOrNull("GoalKeeper"))
        assertNull(Position.fromCsvTokenOrNull("striker"))
        assertNull(Position.fromCsvTokenOrNull(""))
    }

    @Test
    fun `russian names`() {
        assertEquals("нападающий", Position.FORWARD.russianName)
        assertEquals("полузащитник", Position.MIDFIELD.russianName)
        assertEquals("защитник", Position.DEFENDER.russianName)
        assertEquals("вратарь", Position.GOALKEEPER.russianName)
    }

    @Test
    fun `dirty dataset is parsed without exceptions and every row is accounted for`() {
        val result = CsvParser.parseResource("fakePlayersDirty.csv")
        assertEquals(250, result.players.size + result.skippedCount)
        assertTrue(result.skippedCount > 0)
        assertTrue(result.players.any { it.agency == null })
    }

    @Test
    fun `clean dataset has no skipped lines`() {
        val result = CsvParser.parseResource("fakePlayers.csv")
        assertEquals(0, result.skippedCount)
        assertEquals(250, result.players.size)
    }
}
