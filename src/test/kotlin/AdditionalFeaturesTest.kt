import filter.And
import filter.ByPosition
import filter.CheaperThan
import filter.Not
import filter.Or
import filter.matches
import model.Player
import model.Position
import model.Team
import parser.StreamingPlayerSearch
import report.report
import java.io.StringReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AdditionalFeaturesTest {
    @Test
    fun `team aggregates are calculated from its players`() {
        val team = Team("Team", "City")
        player("A", team, Position.FORWARD, 10, red = 1)
        player("B", team, Position.DEFENDER, 30, red = 3)

        assertEquals(40L, team.totalTransferCost)
        assertEquals(2.0, team.averageRedCards)
        assertEquals(2, team.players.size)
    }

    @Test
    fun `sealed filters can be composed recursively`() {
        val team = Team("Team", "City")
        val forward = player("A", team, Position.FORWARD, 40)
        val defender = player("B", team, Position.DEFENDER, 80)
        val filter = Or(
            And(ByPosition(Position.FORWARD), CheaperThan(50)),
            Not(CheaperThan(70)),
        )

        assertTrue(filter.matches(forward))
        assertTrue(filter.matches(defender))
        assertFalse(ByPosition(Position.GOALKEEPER).matches(forward))
    }

    @Test
    fun `streaming search returns first matching valid player`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            Broken;Team;City;FORWARD;Germany;Agency;oops;30;1;1;0;0
            Carded;Team;City;FORWARD;Germany;Agency;60000000;30;1;1;1;0
            Winner;Team;City;DEFENDER;Germany;;70000000;25;1;1;0;0
            Later;Team;City;FORWARD;Germany;Agency;80000000;30;1;1;0;0
        """.trimIndent()

        val result = StreamingPlayerSearch.findFirstEligible(StringReader(csv))

        assertNotNull(result)
        assertEquals("Winner", result.name)
        assertEquals(null, result.agency)
    }

    @Test
    fun `report DSL renders nested sections`() {
        val actual = report {
            metric("Players") { 2 }
            section("Attack") {
                metric("Goals") { 5 }
            }
        }

        assertEquals("Players: 2${System.lineSeparator()}Attack${System.lineSeparator()}  Goals: 5", actual)
    }

    private fun player(name: String, team: Team, position: Position, cost: Int, red: Int = 0) = Player(
        name,
        team,
        position,
        "Country",
        "Agency",
        cost,
        1,
        0,
        0,
        0,
        red,
    )
}
