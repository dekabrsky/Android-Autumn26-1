import filter.And
import filter.ByPosition
import filter.CheaperThan
import filter.Not
import filter.Or
import model.Player
import model.Position
import model.Team
import parser.CsvParser
import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdditionalTasksTest {

    @Test
    fun `task 9 team calculates lazy aggregate values`() {
        val baseTeam = Team("Team", "City")
        val players = listOf(
            player("First", baseTeam, Position.FORWARD, transferCost = 100, redCards = 1),
            player("Second", baseTeam, Position.DEFENDER, transferCost = 250, redCards = 3),
        )
        val aggregateTeam = Team(baseTeam.name, baseTeam.city, players)

        assertEquals(350L, aggregateTeam.totalTransferCost)
        assertEquals(2.0, aggregateTeam.averageRedCards)
    }

    @Test
    fun `task 10 streaming lookup returns first matching player`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            Too cheap;Team;City;FORWARD;Germany;;50000000;25;1;0;0;0
            Broken;Team;City;FORWARD;Germany;;not-a-number;25;1;0;0;0
            First match;Team;City;FORWARD;Germany;;50000001;25;1;0;0;0
            Second match;Team;City;FORWARD;Germany;;70000000;29;1;0;0;0
        """.trimIndent()

        val result = CsvParser.findFirstExpensiveExperiencedPlayerWithoutCards(
            ByteArrayInputStream(csv.toByteArray()),
        )

        assertEquals("First match", result?.name)
    }

    @Test
    fun `task 11 filters can be combined recursively`() {
        val forward = player("Forward", position = Position.FORWARD, transferCost = 100)
        val defender = player("Defender", position = Position.DEFENDER, transferCost = 200)
        val forwardFilter = ByPosition(Position.FORWARD)
        val cheapFilter = CheaperThan(150)

        assertTrue(And(forwardFilter, cheapFilter).matches(forward))
        assertFalse(And(forwardFilter, cheapFilter).matches(defender))
        assertTrue(Or(forwardFilter, cheapFilter).matches(forward))
        assertFalse(Or(forwardFilter, cheapFilter).matches(defender))
        assertTrue(Not(forwardFilter).matches(defender))
        assertFalse(Not(forwardFilter).matches(forward))
    }

    private fun player(
        name: String,
        team: Team = Team("Team", "City"),
        position: Position,
        transferCost: Int,
        redCards: Int = 0,
    ): Player = Player(
        name = name,
        team = team,
        position = position,
        nationality = "Germany",
        agency = null,
        transferCost = transferCost,
        participations = 25,
        goals = 0,
        assists = 0,
        yellowCards = 0,
        redCards = redCards,
    )
}
