import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import model.Position
import parser.CsvParser

class CsvParserTest {
    private val header = "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards"

    private fun row(
        name: String = "Valid",
        team: String = "Team",
        city: String = "City",
        position: String = "FORWARD",
        nationality: String = "Germany",
        agency: String = "Agency",
        cost: String = "100",
        participations: String = "1",
        goals: String = "2",
        assists: String = "3",
        yellow: String = "4",
        red: String = "5",
    ) = listOf(name, team, city, position, nationality, agency, cost, participations, goals, assists, yellow, red)
        .joinToString(";")

    @Test
    fun `empty agency becomes null and text values are trimmed`() {
        val result = CsvParser.parse(
            listOf(header, "  Anna  ;  Team A ; City ;  midfield  ; Germany ;   ; 100 ; 2 ; 3 ; 4 ; 5 ; 6 "),
        )

        assertEquals(0, result.skippedCount)
        assertEquals(1, result.players.size)
        assertEquals("Anna", result.players.single().name)
        assertEquals("Team A", result.players.single().team.name)
        assertNull(result.players.single().agency)
    }

    @Test
    fun `positions are recognized regardless of case and surrounding spaces`() {
        assertEquals(Position.FORWARD, Position.fromCsvTokenOrNull(" forward "))
        assertEquals(Position.MIDFIELD, Position.fromCsvTokenOrNull("MiDfIeLd"))
        assertEquals(Position.DEFENDER, Position.fromCsvTokenOrNull(" DEFENDER"))
        assertEquals(Position.GOALKEEPER, Position.fromCsvTokenOrNull("goalkeeper "))
    }

    @Test
    fun `broken records are skipped and counted`() {
        val result = CsvParser.parse(
            listOf(
                header,
                "Valid;Team;City;FORWARD;Germany;Agency;100;1;2;3;4;5",
                "No cost;Team;City;FORWARD;Germany;Agency;;1;2;3;4;5",
                "Bad goals;Team;City;FORWARD;Germany;Agency;100;1;two;3;4;5",
                "No city;Team;;FORWARD;Germany;Agency;100;1;2;3;4;5",
                "Unknown position;Team;City;STRIKER;Germany;Agency;100;1;2;3;4;5",
            ),
        )

        assertEquals(listOf("Valid"), result.players.map { it.name })
        assertEquals(4, result.skippedCount)
    }

    @Test
    fun `skips rows with empty required text fields`() {
        val result = CsvParser.parse(
            listOf(
                header,
                row(name = ""),
                row(team = ""),
                row(city = ""),
                row(nationality = ""),
                row(position = ""),
            ),
        )

        assertEquals(0, result.players.size)
        assertEquals(5, result.skippedCount)
    }

    @Test
    fun `skips rows with empty or non numeric numeric fields`() {
        val result = CsvParser.parse(
            listOf(
                header,
                row(participations = ""),
                row(assists = "abc"),
                row(yellow = ""),
                row(red = "abc"),
                row(cost = "abc"),
                row(goals = ""),
            ),
        )

        assertEquals(0, result.players.size)
        assertEquals(6, result.skippedCount)
    }

    @Test
    fun `skips rows with unexpected field count`() {
        val result = CsvParser.parse(
            listOf(
                header,
                row(assists = "3", yellow = "4"), // 12 fields — valid control row
                "TooFew;Team;City;FORWARD;Germany;Agency;100;1;2",
                "TooMany;Team;City;FORWARD;Germany;Agency;100;1;2;3;4;5;6",
            ),
        )

        assertEquals(listOf("Valid"), result.players.map { it.name })
        assertEquals(2, result.skippedCount)
    }
}
