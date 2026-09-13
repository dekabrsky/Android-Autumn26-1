import model.Position
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CsvParserTest {

    @Test
    fun `empty agency becomes null`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            Test Player;Team A;City A;FORWARD;Germany;;1000;10;5;2;1;0
        """.trimIndent()

        val result = CsvParser.parse(csv)
        assertEquals(0, result.skippedCount)
        assertEquals(1, result.players.size)
        assertNull(result.players[0].agency)
    }

    @Test
    fun `position is recognized ignoring case and spaces`() {
        assertEquals(Position.FORWARD, Position.fromCsvTokenOrNull("  forward  "))
        assertEquals(Position.MIDFIELD, Position.fromCsvTokenOrNull("MidField"))
        assertEquals(Position.DEFENDER, Position.fromCsvTokenOrNull("defender"))
        assertEquals(Position.GOALKEEPER, Position.fromCsvTokenOrNull(" GOALKEEPER "))
        assertNull(Position.fromCsvTokenOrNull("striker"))
    }

    @Test
    fun `broken numeric fields are skipped and counted`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            Good Player;Team A;City A;FORWARD;Germany;Agency X;1000;10;5;2;1;0
            Bad Cost;Team A;City A;FORWARD;Germany;Agency X;;10;5;2;1;0
            Bad Goals;Team A;City A;DEFENDER;Germany;Agency X;2000;10;;2;1;0
        """.trimIndent()

        val result = CsvParser.parse(csv)
        assertEquals(1, result.players.size)
        assertEquals(2, result.skippedCount)
        assertEquals("Good Player", result.players[0].name)
    }

    @Test
    fun `empty required text fields are skipped`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            ;Team A;City A;FORWARD;Germany;Agency X;1000;10;5;2;1;0
            Player; ;City A;FORWARD;Germany;Agency X;1000;10;5;2;1;0
            Player;Team A;;FORWARD;Germany;Agency X;1000;10;5;2;1;0
            Ok Player;Team A;City A;MIDFIELD;Brazil;Agency Y;500;8;1;3;0;0
        """.trimIndent()

        val result = CsvParser.parse(csv)
        assertEquals(1, result.players.size)
        assertEquals(3, result.skippedCount)
        assertEquals("Ok Player", result.players[0].name)
    }

    @Test
    fun `text fields are trimmed`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
              Anna  ;  Team B  ;  City B  ;  defender  ;  Spain  ;  Agency Z  ;3000;12;4;1;2;0
        """.trimIndent()

        val result = CsvParser.parse(csv)
        val p = result.players.single()
        assertEquals("Anna", p.name)
        assertEquals("Team B", p.team.name)
        assertEquals("City B", p.team.city)
        assertEquals(Position.DEFENDER, p.position)
        assertEquals("Spain", p.nationality)
        assertEquals("Agency Z", p.agency)
    }

    @Test
    fun `dirty dataset parses without crash`() {
        val stream = javaClass.classLoader.getResourceAsStream("fakePlayersDirty.csv")
        assertNotNull(stream)
        val result = CsvParser.parse(stream!!)
        assertTrue(result.players.isNotEmpty())
        assertTrue(result.skippedCount >= 0)
        assertEquals(250, result.players.size + result.skippedCount)
    }
}
