import model.Position
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CsvParserTest {
    @Test
    fun `empty agency is mapped to null and player is kept`() {
        val result = CsvParser.parse(
            listOf(
                HEADER,
                "Alex Smith;Nevada whales;South Carolina;FORWARD;Germany;;1000000;10;5;3;1;0",
            ),
        )

        assertEquals(1, result.players.size)
        assertEquals(0, result.skippedCount)
        assertNull(result.players.single().agency)
    }

    @Test
    fun `position ignores case and extra spaces`() {
        val result = CsvParser.parse(
            listOf(
                HEADER,
                "Alex Smith;Nevada whales;South Carolina; defender ;Germany;Best Agency;1000000;10;5;3;1;0",
            ),
        )

        assertEquals(Position.DEFENDER, result.players.single().position)
    }

    @Test
    fun `text fields are trimmed`() {
        val result = CsvParser.parse(
            listOf(
                HEADER,
                " Alex Smith ; Nevada whales ; South Carolina ;MIDFIELD; Germany ; Best Agency ;1000000;10;5;3;1;0",
            ),
        )

        val player = result.players.single()
        assertEquals("Alex Smith", player.name)
        assertEquals("Nevada whales", player.team.name)
        assertEquals("South Carolina", player.team.city)
        assertEquals("Germany", player.nationality)
        assertEquals("Best Agency", player.agency)
    }

    @Test
    fun `broken rows are skipped and counted`() {
        val result = CsvParser.parse(
            listOf(
                HEADER,
                "Alex Smith;Nevada whales;South Carolina;FORWARD;Germany;Best Agency;1000000;10;5;3;1;0",
                "No Team;;South Carolina;FORWARD;Germany;Best Agency;1000000;10;5;3;1;0",
                "No Position;Nevada whales;South Carolina;;Germany;Best Agency;1000000;10;5;3;1;0",
                "Bad Cost;Nevada whales;South Carolina;FORWARD;Germany;Best Agency;bad;10;5;3;1;0",
                "Bad Goals;Nevada whales;South Carolina;FORWARD;Germany;Best Agency;1000000;10;;3;1;0",
                "No Name;Nevada whales;South Carolina;FORWARD;Germany;Best Agency;1000000;10;5;3;1",
            ),
        )

        assertEquals(1, result.players.size)
        assertEquals(5, result.skippedCount)
    }

    @Test
    fun `all required text and numeric mapping errors skip rows`() {
        val result = CsvParser.parse(
            listOf(
                HEADER,
                "Valid Player;Nevada whales;South Carolina;FORWARD;Germany;Best Agency;1000000;10;5;3;1;0",
                ";Nevada whales;South Carolina;FORWARD;Germany;Best Agency;1000000;10;5;3;1;0",
                "No City;Nevada whales;;FORWARD;Germany;Best Agency;1000000;10;5;3;1;0",
                "No Nationality;Nevada whales;South Carolina;FORWARD;;Best Agency;1000000;10;5;3;1;0",
                "Unknown Position;Nevada whales;South Carolina;COACH;Germany;Best Agency;1000000;10;5;3;1;0",
                "Bad Participations;Nevada whales;South Carolina;FORWARD;Germany;Best Agency;1000000;ten;5;3;1;0",
                "Bad Assists;Nevada whales;South Carolina;FORWARD;Germany;Best Agency;1000000;10;5;many;1;0",
                "Bad Yellow Cards;Nevada whales;South Carolina;FORWARD;Germany;Best Agency;1000000;10;5;3;yellow;0",
                "Bad Red Cards;Nevada whales;South Carolina;FORWARD;Germany;Best Agency;1000000;10;5;3;1;red",
            ),
        )

        assertEquals(listOf("Valid Player"), result.players.map { it.name })
        assertEquals(8, result.skippedCount)
    }

    @Test
    fun `players share team data with name and city`() {
        val result = CsvParser.parse(
            listOf(
                HEADER,
                "Alex Smith;Nevada whales;South Carolina;FORWARD;Germany;Best Agency;1000000;10;5;3;1;0",
                "Bob Stone;Nevada whales;South Carolina;DEFENDER;Brazil;Best Agency;2000000;12;1;2;0;2",
                "Carol Black;Utah nemesis;New York;MIDFIELD;Brazil;Best Agency;3000000;8;2;2;0;4",
            ),
        )

        val firstPlayer = result.players[0]
        val secondPlayer = result.players[1]
        val team = firstPlayer.team

        assertSame(team, secondPlayer.team)
        assertEquals("Nevada whales", team.name)
        assertEquals("South Carolina", team.city)
    }

    @Test
    fun `parse records returns valid csv records without building teams`() {
        val records = CsvParser.parseRecords(
            sequenceOf(
                HEADER,
                "Alex Smith;Nevada whales;South Carolina;FORWARD;Germany;;1000000;10;5;3;1;0",
                "Bad Cost;Nevada whales;South Carolina;FORWARD;Germany;Best Agency;bad;10;5;3;1;0",
            ),
        ).toList()

        assertEquals(1, records.size)
        assertEquals("Alex Smith", records.single().name)
        assertEquals("Nevada whales", records.single().teamName)
        assertNull(records.single().agency)
    }

    @Test
    fun `finds first expensive experienced player without cards in stream`() {
        val record = CsvParser.findFirstExpensiveExperiencedPlayerWithoutCards(
            sequenceOf(
                HEADER,
                "Too Cheap;Nevada whales;South Carolina;FORWARD;Germany;Best Agency;50000000;30;5;3;0;0",
                "Has Yellow Card;Nevada whales;South Carolina;FORWARD;Germany;Best Agency;60000000;30;5;3;1;0",
                "Expected Player;Utah nemesis;New York;DEFENDER;Brazil;Best Agency;60000000;25;1;2;0;0",
                "Later Player;Utah nemesis;New York;MIDFIELD;Brazil;Best Agency;70000000;30;1;2;0;0",
            ),
        )

        assertNotNull(record)
        assertEquals("Expected Player", record.name)
    }

    @Test
    fun `stream search stops reading after first matching record`() {
        val lines = sequence {
            yield(HEADER)
            yield("Expected Player;Utah nemesis;New York;DEFENDER;Brazil;Best Agency;60000000;25;1;2;0;0")
            error("The stream should not be consumed after the first matching record")
        }

        val record = CsvParser.findFirstExpensiveExperiencedPlayerWithoutCards(lines)

        assertNotNull(record)
        assertEquals("Expected Player", record.name)
    }

    @Test
    fun `clean dataset is parsed without skipped rows`() {
        val result = CsvParser.parse(readResourceLines("fakePlayers.csv"))

        assertEquals(250, result.players.size)
        assertEquals(0, result.skippedCount)
    }

    @Test
    fun `dirty dataset is parsed without falling and counts skipped rows`() {
        val result = CsvParser.parse(readResourceLines("fakePlayersDirty.csv"))

        assertEquals(250, result.players.size + result.skippedCount)
        assertTrue(result.players.isNotEmpty())
        assertTrue(result.skippedCount > 0)
    }

    private companion object {
        const val HEADER = "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards"

        fun readResourceLines(name: String): List<String> =
            requireNotNull(CsvParserTest::class.java.classLoader.getResourceAsStream(name))
                .bufferedReader()
                .use { it.readLines() }
    }
}
