import model.Position
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CsvParserTest {

    @Test
    fun `empty agency becomes null and player is not skipped`() {
        val result = CsvParser.parse(
            sequenceOf(
                HEADER,
                row(agency = ""),
            ),
        )

        assertEquals(1, result.players.size)
        assertEquals(0, result.skippedCount)
        assertNull(result.players.single().agency)
    }

    @Test
    fun `position mapping ignores case and surrounding spaces`() {
        val tokens = listOf(" forward ", "MiDfIeLd", " defender", "GOALKEEPER ")
        val result = CsvParser.parse(
            sequenceOf(HEADER) + tokens.mapIndexed { index, token ->
                row(name = "Player $index", position = token)
            },
        )

        assertEquals(
            listOf(Position.FORWARD, Position.MIDFIELD, Position.DEFENDER, Position.GOALKEEPER),
            result.players.map { it.position },
        )
        assertEquals(0, result.skippedCount)
    }

    @Test
    fun `broken rows are skipped and counted`() {
        val result = CsvParser.parse(
            sequenceOf(
                HEADER,
                row(name = "Valid"),
                row(name = "No cost", transferCost = ""),
                row(name = "Bad goals", goals = "many"),
                row(name = "No team", team = "   "),
                row(name = "Unknown position", position = "COACH"),
            ),
        )

        assertEquals(listOf("Valid"), result.players.map { it.name })
        assertEquals(4, result.skippedCount)
    }

    @Test
    fun `text fields are trimmed`() {
        val player = CsvParser.parseLineOrNull(
            row(
                name = "  Alex  ",
                team = "  Tigers ",
                city = " City  ",
                nationality = " Germany ",
                agency = " Agency  ",
            ),
        )!!

        assertEquals("Alex", player.name)
        assertEquals("Tigers", player.team.name)
        assertEquals("City", player.team.city)
        assertEquals("Germany", player.nationality)
        assertEquals("Agency", player.agency)
    }

    private fun row(
        name: String = "Player",
        team: String = "Team",
        city: String = "City",
        position: String = "FORWARD",
        nationality: String = "Germany",
        agency: String = "Agency",
        transferCost: String = "1000000",
        participations: String = "10",
        goals: String = "2",
        assists: String = "3",
        yellowCards: String = "1",
        redCards: String = "0",
    ): String = listOf(
        name,
        team,
        city,
        position,
        nationality,
        agency,
        transferCost,
        participations,
        goals,
        assists,
        yellowCards,
        redCards,
    ).joinToString(";")

    companion object {
        private const val HEADER =
            "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;" +
                "Goals;Assists;Yellow cards;Red cards"
    }
}
