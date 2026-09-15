import model.Position
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CsvParserTest {
    @Test
    fun `empty agency becomes null and player is kept`() {
        val line = "John Doe;Team A;City A;FORWARD;Russia;;100;10;5;3;1;0"
        val player = CsvParser.parseLine(line)

        assertNotNull(player)
        assertNull(player.agency)
        assertEquals("John Doe", player.name)
        assertEquals("Team A", player.team.name)
        assertEquals("City A", player.team.city)
    }

    @Test
    fun `non-empty agency is preserved as-is`() {
        val line = "John Doe;Team A;City A;FORWARD;Russia;Some Agency;100;10;5;3;1;0"
        val player = CsvParser.parseLine(line)

        assertNotNull(player)
        assertEquals("Some Agency", player.agency)
    }


    @Test
    fun `position is recognized in any case and with spaces`() {
        assertEquals(Position.MIDFIELD,   Position.fromCsvTokenOrNull("midfield"))
        assertEquals(Position.MIDFIELD,   Position.fromCsvTokenOrNull("MIDFIELD"))
        assertEquals(Position.MIDFIELD,   Position.fromCsvTokenOrNull("  MidField  "))
        assertEquals(Position.DEFENDER,   Position.fromCsvTokenOrNull("DEFENDER"))
        assertEquals(Position.FORWARD,    Position.fromCsvTokenOrNull("forward"))
        assertEquals(Position.GOALKEEPER, Position.fromCsvTokenOrNull(" Goalkeeper "))
    }

    @Test
    fun `unknown position returns null`() {
        assertNull(Position.fromCsvTokenOrNull("striker"))
        assertNull(Position.fromCsvTokenOrNull(""))
        assertNull(Position.fromCsvTokenOrNull("   "))
    }

    @Test
    fun `russian names are defined for all positions`() {
        assertEquals("нападающий",   Position.FORWARD.russianName)
        assertEquals("полузащитник", Position.MIDFIELD.russianName)
        assertEquals("защитник",     Position.DEFENDER.russianName)
        assertEquals("вратарь",      Position.GOALKEEPER.russianName)
    }

    @Test
    fun `non-numeric transfer cost skips the line`() {
        val line = "John Doe;Team A;City A;FORWARD;Russia;Agency;abc;10;5;3;1;0"
        assertNull(CsvParser.parseLine(line))
    }

    @Test
    fun `empty transfer cost skips the line`() {
        val line = "John Doe;Team A;City A;FORWARD;Russia;Agency;;10;5;3;1;0"
        assertNull(CsvParser.parseLine(line))
    }

    @Test
    fun `empty goals skips the line`() {
        val line = "John Doe;Team A;City A;FORWARD;Russia;Agency;100;10;;3;1;0"
        assertNull(CsvParser.parseLine(line))
    }

    @Test
    fun `empty participations skips the line`() {
        val line = "John Doe;Team A;City A;FORWARD;Russia;Agency;100;;5;3;1;0"
        assertNull(CsvParser.parseLine(line))
    }

    @Test
    fun `empty assists skips the line`() {
        val line = "John Doe;Team A;City A;FORWARD;Russia;Agency;100;10;5;;1;0"
        assertNull(CsvParser.parseLine(line))
    }

    @Test
    fun `empty yellow cards skips the line`() {
        val line = "John Doe;Team A;City A;FORWARD;Russia;Agency;100;10;5;3;;0"
        assertNull(CsvParser.parseLine(line))
    }

    @Test
    fun `empty red cards skips the line`() {
        val line = "John Doe;Team A;City A;FORWARD;Russia;Agency;100;10;5;3;1;"
        assertNull(CsvParser.parseLine(line))
    }

    @Test
    fun `empty name skips the line`() {
        assertNull(CsvParser.parseLine(";Team A;City A;FORWARD;Russia;Agency;100;10;5;3;1;0"))
    }

    @Test
    fun `empty team skips the line`() {
        assertNull(CsvParser.parseLine("John Doe;;City A;FORWARD;Russia;Agency;100;10;5;3;1;0"))
    }

    @Test
    fun `empty city skips the line`() {
        assertNull(CsvParser.parseLine("John Doe;Team A;;FORWARD;Russia;Agency;100;10;5;3;1;0"))
    }

    @Test
    fun `empty position skips the line`() {
        assertNull(CsvParser.parseLine("John Doe;Team A;City A;;Russia;Agency;100;10;5;3;1;0"))
    }

    @Test
    fun `empty country skips the line`() {
        assertNull(CsvParser.parseLine("John Doe;Team A;City A;FORWARD;;Agency;100;10;5;3;1;0"))
    }

    @Test
    fun `too short line is skipped`() {
        assertNull(CsvParser.parseLine("John Doe;Team A;City A;FORWARD"))
    }

    @Test
    fun `text fields are trimmed`() {
        val line = "  John Doe  ;  Team A  ;  City A  ;  forward  ;  Russia  ;  Agency  ;100;10;5;3;1;0"
        val player = CsvParser.parseLine(line)

        assertNotNull(player)
        assertEquals("John Doe", player.name)
        assertEquals("Team A", player.team.name)
        assertEquals("City A", player.team.city)
        assertEquals("Russia", player.country)
        assertEquals("Agency", player.agency)
        assertEquals(Position.FORWARD, player.position)
    }

    @Test
    fun `parser counts skipped lines in small inline csv`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            John Doe;Team A;City A;FORWARD;Russia;Agency;100;10;5;3;1;0
            ;Team A;City A;FORWARD;Russia;Agency;100;10;5;3;1;0
            Jane Doe;Team B;City B;DEFENDER;Germany;;200;20;0;0;0;0
            Bad Row;Team C;City C;FORWARD;Russia;Agency;abc;5;1;1;0;0
        """.trimIndent()

        val result = csv.reader().buffered().use { CsvParser.parse(it) }

        assertEquals(2, result.players.size)
        assertEquals(2, result.skippedCount)
    }

    @Test
    fun `header line is ignored and not counted as skipped`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            John Doe;Team A;City A;FORWARD;Russia;;100;10;5;3;1;0
        """.trimIndent()

        val result = csv.reader().buffered().use { CsvParser.parse(it) }
        assertEquals(1, result.players.size)
        assertEquals(0, result.skippedCount)
    }

    @Test
    fun `empty lines in csv are ignored`() {
        val csv = """

            John Doe;Team A;City A;FORWARD;Russia;;100;10;5;3;1;0

            Jane Doe;Team B;City B;DEFENDER;Germany;;200;20;0;0;0;0

        """.trimIndent()

        val result = csv.reader().buffered().use { CsvParser.parse(it) }
        assertEquals(2, result.players.size)
        assertEquals(0, result.skippedCount)
    }

    @Test
    fun `clean dataset has zero skipped and 250 players`() {
        val result = CsvParser.parseResource("fakePlayers.csv")
        assertEquals(250, result.players.size)
        assertEquals(0, result.skippedCount)
    }

    @Test
    fun `dirty dataset is parsed without crashes and counts skips`() {
        val result = CsvParser.parseResource("fakePlayersDirty.csv")
        assertEquals(220, result.players.size)
        assertEquals(30, result.skippedCount)
    }
}