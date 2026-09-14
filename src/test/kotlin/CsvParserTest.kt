import model.Position
import model.Team
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CsvParserTest {
    private val row = "Alex;Stars;Perm;FORWARD;Russia;Agency;75012006;22;19;6;3;0"

    @Test
    fun `maps all fields and trims text`() {
        val player = assertNotNull(CsvParser.parseLineOrNull(
            row.split(';').joinToString(";") { "  $it  " }
        ))
        assertEquals("Alex", player.name)
        assertEquals(Team("Stars", "Perm"), player.team)
        assertEquals(Position.FORWARD, player.position)
        assertEquals("Russia", player.country)
        assertEquals("Agency", player.agency)
        assertEquals(75012006L, player.transferCost)
        assertEquals(listOf(22, 19, 6, 3, 0), listOf(
            player.participations, player.goals, player.assists, player.yellowCards, player.redCards
        ))
    }

    @Test
    fun `empty agency keeps player`() {
        val player = assertNotNull(CsvParser.parseLineOrNull(row.replace("Agency", "  ")))
        assertNull(player.agency)
    }

    @Test
    fun `positions ignore case and surrounding whitespace`() {
        for (position in Position.entries) {
            assertEquals(position, Position.fromCsvTokenOrNull(" \t${position.name.lowercase()}  "))
            assertEquals(position, CsvParser.parseLineOrNull(
                row.replace("FORWARD", " ${position.name.lowercase()} ")
            )?.position)
        }
        assertNull(Position.fromCsvTokenOrNull("unknown"))
        assertNull(Position.fromCsvTokenOrNull(" "))
        assertEquals(listOf("нападающий", "полузащитник", "защитник", "вратарь"),
            Position.entries.map { it.russianName })
    }

    @Test
    fun `invalid fields and malformed rows are skipped and counted`() {
        val invalid = mutableListOf("", "Alex;Stars", "$row;extra")
        for (index in listOf(0, 1, 2, 3, 4) + (6..11)) {
            invalid.add(row.split(';').toMutableList().also { it[index] = " " }.joinToString(";"))
        }
        for (index in 6..11) {
            for (value in listOf("oops", "1.5", "999999999999999999999999")) {
                invalid.add(row.split(';').toMutableList().also { it[index] = value }.joinToString(";"))
            }
        }
        invalid.add(row.replace("FORWARD", "unknown"))
        val input = (listOf(row) + invalid + row.replace("Agency", "")).joinToString("\n")
        val result = CsvParser.parse(input.reader(), hasHeader = false)
        assertEquals(2, result.players.size)
        assertEquals(invalid.size, result.skippedCount)
        assertNull(result.players.last().agency)
    }

    @Test
    fun `empty input has no skipped rows`() {
        assertEquals(0, CsvParser.parse("".reader()).skippedCount)
        assertEquals(emptyList(), CsvParser.parse("".reader()).players)
    }

    @Test
    fun `bundled datasets parse without losing rows silently`() {
        for ((name, expectedPlayers) in listOf("fakePlayers.csv" to 250, "fakePlayersDirty.csv" to 220)) {
            val stream = assertNotNull(javaClass.classLoader.getResourceAsStream(name))
            val result = stream.bufferedReader().use { CsvParser.parse(it) }
            assertEquals(250, result.players.size + result.skippedCount)
            assertEquals(expectedPlayers, result.players.size)
            assertEquals(250 - expectedPlayers, result.skippedCount)
        }
    }
}
