import model.Position
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CsvParserTest {

    private val csvHeader = "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards"

    @Test
    fun `should parse player correctly when agency is missing`() {
        val rawRecord = makeRow(name = "Player Alpha", agency = "")
        val result = CsvParser.parseLine(rawRecord)

        assertNotNull(result)
        assertNull(result.agency)
        assertEquals("Player Alpha", result.name)
    }

    @Test
    fun `should match valid position strings`() {
        val validInputs = arrayListOf("MIDFIELD", "  GOALKEEPER  ", "FORWARD")
        for (value in validInputs) {
            assertNotNull(Position.fromCsvTokenOrNull(value), "Token failed: $value")
        }

        assertEquals(Position.MIDFIELD, Position.fromCsvTokenOrNull("MIDFIELD"))
        assertEquals(Position.GOALKEEPER, Position.fromCsvTokenOrNull("  GOALKEEPER  "))
    }

    @Test
    fun `should return null for unrecognized position strings`() {
        val badTokens = listOf("COACH", "REFEREE", "", "   ", "123")
        badTokens.forEach { token ->
            assertNull(Position.fromCsvTokenOrNull(token), "Token should be null: $token")
        }
    }

    @Test
    fun `should strip whitespace from text attributes`() {
        val inputLine = makeRow(name = "  Player Beta ")
        val parsed = CsvParser.parseLine(inputLine)

        assertNotNull(parsed)
        assertEquals("Player Beta", parsed.name)
    }

    @Test
    fun `should ignore record with invalid position`() {
        val entry = makeRow(position = "UNKNOWN_POS")
        assertNull(CsvParser.parseLine(entry))
    }

    @Test
    fun `should ignore record with blank mandatory name`() {
        val entry = makeRow(name = "   ")
        assertNull(CsvParser.parseLine(entry))
    }

    @Test
    fun `should ignore record with non-numeric transfer cost`() {
        val entry = makeRow(cost = "invalid_cost")
        assertNull(CsvParser.parseLine(entry))
    }

    @Test
    fun `should process batch correctly and track skipped entries`() {
        val inputBatch = listOf(
            csvHeader,
            makeRow(name = "Player One", position = "GOALKEEPER", team = "Team A"),
            makeRow(name = "Player Two", position = "INVALID_POS"),
            makeRow(name = "Player Three", position = "FORWARD", agency = "", cost = "50000000")
        )
        val outcome = CsvParser.parse(inputBatch)

        assertEquals(2, outcome.players.size)
        assertEquals(1, outcome.skippedCount)
    }

    private fun makeRow(
        name: String = "Default Player",
        team: String = "Default Team",
        city: String = "Default City",
        position: String = "MIDFIELD",
        nationality: String = "Default Country",
        agency: String = "Default Agency",
        cost: String = "10000000"
    ): String {
        return listOf(name, team, city, position, nationality, agency, cost, "20", "5", "3", "1", "0")
            .joinToString(";")
    }
}