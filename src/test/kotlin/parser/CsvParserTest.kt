package parser

import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class CsvParserTest {

    private val TEST_CSV_PATH = "src/test/kotlin/resources/fakePlayersTest.csv"

    @Test
    fun `parse correct player`() {
        val result = CsvParser.readCsv(TEST_CSV_PATH)
        assertNotNull(result)

        val player = result.players[0]
        assertNotNull(player)
        assertEquals("Correct Person", player.name)
        assertEquals("Russia", player.nationality)
        assertEquals("Agency A", player.agency)
        assertEquals(1000000, player.transferCost)
        assertEquals(10, player.participations)
        assertEquals(5, player.goals)
        assertEquals(3, player.assists)
        assertEquals(2, player.yellowCards)
        assertEquals(1, player.redCards)

        val playerTeam = player.team
        assertNotNull(playerTeam)
        assertEquals("Team A", playerTeam.name)
        assertEquals("Moscow", playerTeam.city)

        val playerPosition = player.position
        assertNotNull(playerPosition)
        assertEquals("FORWARD", player.position.name)
        assertEquals("нападающий", player.position.russianName)
    }

    @Test
    fun `parse empty agency as null`() {
        val result = CsvParser.readCsv(TEST_CSV_PATH)
        assertNotNull(result)

        val player = result.players[1]
        assertNotNull(player)
        assertNull(player.agency)
    }

    @Test
    fun `parse blank agency as null`() {
        val result = CsvParser.readCsv(TEST_CSV_PATH)
        assertNotNull(result)

        val player = result.players[2]
        assertNotNull(player)
        assertNull(player.agency)
    }

    @Test
    fun `parse position with spaces and different case`() {
        val result = CsvParser.readCsv(TEST_CSV_PATH)
        assertNotNull(result)

        val player = result.players[3]
        assertNotNull(player)

        val playerPosition = player.position
        assertNotNull(playerPosition)
        assertEquals("FORWARD", playerPosition.name)
        assertEquals("нападающий", playerPosition.russianName)
    }

    @Test
    fun `trim spaces from text and numeric fields`() {
        val result = CsvParser.readCsv(TEST_CSV_PATH)
        assertNotNull(result)

        val player = result.players[4]
        assertNotNull(player)
        assertEquals("Fields With Spaces Person", player.name)
        assertEquals("Russia", player.nationality)
        assertEquals("Agency A", player.agency)
        assertEquals(1000000, player.transferCost)
        assertEquals(10, player.participations)
        assertEquals(5, player.goals)
        assertEquals(3, player.assists)
        assertEquals(2, player.yellowCards)
        assertEquals(1, player.redCards)

        val playerTeam = player.team
        assertNotNull(playerTeam)
        assertEquals("Team A", playerTeam.name)
        assertEquals("Moscow", playerTeam.city)

        val playerPosition = player.position
        assertNotNull(playerPosition)
        assertEquals("FORWARD", player.position.name)
        assertEquals("нападающий", player.position.russianName)
    }

    @Test
    fun `skip invalid rows and count skipped rows`() {
        val result = CsvParser.readCsv(TEST_CSV_PATH)

        assertNotNull(result)
        assertEquals(17, result.skippedCount)
    }
}