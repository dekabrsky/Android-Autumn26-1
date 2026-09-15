import filter.And
import filter.ByPosition
import filter.CheaperThan
import filter.Not
import filter.Or
import model.Player
import model.Position
import model.Team
import parser.StreamingPlayerSearch
import report.buildReport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AdditionalTasksTest {

    private val team = Team(
        name = "Test team",
        city = "Test city"
    )

    private fun player(
        name: String,
        position: Position = Position.FORWARD,
        cost: Long = 10_000_000L,
        matches: Int = 20,
        yellowCards: Int = 0,
        redCards: Int = 0
    ): Player =
        Player(
            name = name,
            team = team,
            position = position,
            country = "Germany",
            agency = null,
            transferCost = cost,
            matches = matches,
            goals = 0,
            assists = 0,
            yellowCards = yellowCards,
            redCards = redCards
        )

    @Test
    fun `team aggregates players lazily`() {
        val players = listOf(
            player(
                name = "A",
                cost = 10,
                yellowCards = 2,
                redCards = 0
            ),
            player(
                name = "B",
                cost = 30,
                yellowCards = 4,
                redCards = 2
            )
        )

        val aggregatedTeam = Team.aggregate(players).single()

        assertEquals(2, aggregatedTeam.players.size)
        assertEquals(40L, aggregatedTeam.totalTransferCost)
        assertEquals(1.0, aggregatedTeam.averageRedCards)
        assertEquals(4.0, aggregatedTeam.averageCards)
    }

    @Test
    fun `sealed filters can be combined`() {
        val defender = player(
            name = "Defender",
            position = Position.DEFENDER,
            cost = 10
        )

        val expensiveForward = player(
            name = "Forward",
            position = Position.FORWARD,
            cost = 100
        )

        val cheapDefender = And(
            ByPosition(Position.DEFENDER),
            CheaperThan(50)
        )

        assertTrue(cheapDefender.matches(defender))
        assertFalse(cheapDefender.matches(expensiveForward))

        val defenderOrForward = Or(
            ByPosition(Position.DEFENDER),
            ByPosition(Position.FORWARD)
        )

        assertTrue(defenderOrForward.matches(defender))
        assertTrue(defenderOrForward.matches(expensiveForward))

        val notDefender = Not(
            ByPosition(Position.DEFENDER)
        )

        assertFalse(notDefender.matches(defender))
        assertTrue(notDefender.matches(expensiveForward))
    }

    @Test
    fun `streaming search returns first eligible player`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            Too cheap;Team;City;FORWARD;Germany;;40000000;30;1;1;0;0
            First match;Team;City;DEFENDER;Germany;;60000000;25;1;1;0;0
            Second match;Team;City;FORWARD;Germany;;80000000;30;1;1;0;0
        """.trimIndent()

        val result = StreamingPlayerSearch.findFirstEligible(
            csv.byteInputStream()
        )

        assertNotNull(result)
        assertEquals("First match", result.name)
    }

    @Test
    fun `streaming search rejects players with cards`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            Carded;Team;City;FORWARD;Germany;;60000000;30;1;1;1;0
        """.trimIndent()

        val result = StreamingPlayerSearch.findFirstEligible(
            csv.byteInputStream()
        )

        assertNull(result)
    }

    @Test
    fun `report DSL renders metrics and sections`() {
        val result = buildReport {
            metric("Игроков") { 3 }

            section("Атаки") {
                metric("Голов") { 10 }
            }
        }.toString()

        assertTrue(result.contains("Игроков: 3"))
        assertTrue(result.contains("Атаки"))
        assertTrue(result.contains("Голов: 10"))
    }
}