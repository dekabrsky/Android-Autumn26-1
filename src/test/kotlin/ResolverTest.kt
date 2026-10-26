package resolver

import model.Player
import model.Position
import model.Team
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolverTest {

    private lateinit var teamA: Team
    private lateinit var teamB: Team
    private lateinit var teamC: Team

    private lateinit var players: List<Player>
    private lateinit var resolver: Resolver

    @BeforeTest
    fun setUp() {
        teamA = Team("Team A", "City A")
        teamB = Team("Team B", "City B")
        teamC = Team("Team C", "City C")

        players = listOf(
            Player("P1", teamA, Position.FORWARD, "Germany", "Agency A", 100, 10, 5, 5, 1, 1),
            Player("P2", teamA, Position.DEFENDER, "Germany", "Agency A", 200, 20, 10, 10, 2, 2),
            Player("P3", teamB, Position.FORWARD, "Brazil", null, 300, 30, 15, 15, 3, 3),
            Player("P4", teamB, Position.DEFENDER, "Germany", "Agency B", 400, 40, 20, 20, 4, 4),
            Player("P5", teamC, Position.GOALKEEPER, "Spain", "Agency C", 500, 50, 25, 25, 5, 5)
        )

        resolver = Resolver(players)
    }

    @Test
    fun `test getCountWithoutAgency`() {
        assertEquals(1, resolver.getCountWithoutAgency())
    }

    @Test
    fun `test getBestScorerDefender`() {
        val (name, goals) = resolver.getBestScorerDefender()
        assertEquals("P4", name)
        assertEquals(20, goals)
    }

    @Test
    fun `test getTheExpensiveGermanPlayerPosition`() {
        assertEquals("защитник", resolver.getTheExpensiveGermanPlayerPosition())
    }

    @Test
    fun `test getTheRudestTeam`() {
        assertEquals("Team C", resolver.getTheRudestTeam().name)
    }

    @Test
    fun `test getAverageTransferCostByPosition`() {
        val results = resolver.getAverageTransferCostByPosition()
        assertEquals(Position.GOALKEEPER, results.keys.first())
        assertEquals(500.0, results[Position.GOALKEEPER])
        assertEquals(Position.DEFENDER, results.keys.elementAt(1))
        assertEquals(300.0, results[Position.DEFENDER])
        assertEquals(Position.FORWARD, results.keys.last())
        assertEquals(200.0, results[Position.FORWARD])
    }

    @Test
    fun `test getMostValuablePlayers`() {
        val top = resolver.getMostValuablePlayers()
        assertEquals(3, top.size)
        assertEquals("P5", top[0].name)
        assertEquals("P4", top[1].name)
        assertEquals("P3", top[2].name)
    }

    @Test
    fun `test getMostPopularAgencyByCountry`() {
        val results = resolver.getMostPopularAgencyByCountry()
        assertEquals("Agency A", results["Germany"])
        assertEquals("", results["Brazil"])
        assertEquals("Agency C", results["Spain"])
    }

    @Test
    fun `test getGoalsShareByMedianCost`() {
        val (below, above) = resolver.getGoalsShareByMedianCost()
        assertEquals(0.4, below, 0.001)
        assertEquals(0.6, above, 0.001)
    }
}
