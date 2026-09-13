import model.Player
import model.Position
import model.Team
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResolverTest {
    private val teamA = Team("Team A", "City A")
    private val teamB = Team("Team B", "City B")

    private val players = listOf(
        Player("Alex", teamA, Position.DEFENDER, "Germany", null, 100, 10, 5, 2, 1, 1),
        Player("Bob", teamA, Position.FORWARD, "Germany", "Agency X", 300, 12, 8, 4, 2, 3),
        Player("Carl", teamB, Position.DEFENDER, "France", "Agency Y", 200, 8, 10, 1, 0, 0),
        Player("Dan", teamB, Position.MIDFIELD, "France", "Agency Y", 400, 15, 2, 5, 1, 0),
        Player("Eric", teamB, Position.GOALKEEPER, "Spain", "Agency Z", 500, 20, 0, 0, 0, 0)
    )

    private val resolver = Resolver(players)

    @Test
    fun `counts players without agency`() {
        assertEquals(1, resolver.getCountWithoutAgency())
    }

    @Test
    fun `finds best defender scorer`() {
        assertEquals("Carl" to 10, resolver.getBestScorerDefender())
    }

    @Test
    fun `finds position of most expensive german player`() {
        assertEquals("нападающий", resolver.getTheExpensiveGermanPlayerPosition())
    }

    @Test
    fun `finds team with highest average red cards`() {
        assertEquals(teamA, resolver.getTheRudestTeam())
    }

    @Test
    fun `calculates average cost by position in descending order`() {
        val result = resolver.getAverageTransferCostByPosition()

        assertEquals(listOf(Position.GOALKEEPER, Position.MIDFIELD, Position.FORWARD, Position.DEFENDER), result.keys.toList())
        assertEquals(500.0, result[Position.GOALKEEPER])
        assertEquals(150.0, result[Position.DEFENDER])
    }

    @Test
    fun `returns top three valuable players`() {
        val result = resolver.getMostValuablePlayers()

        assertEquals(listOf("Bob", "Carl", "Dan"), result.map { it.name })
    }

    @Test
    fun `finds most popular agency for each country`() {
        val result = resolver.getMostPopularAgencyByCountry()

        assertEquals("Agency X", result["Germany"])
        assertEquals("Agency Y", result["France"])
        assertEquals("Agency Z", result["Spain"])
    }

    @Test
    fun `calculates goals share by median transfer cost`() {
        val result = resolver.getGoalsShareByMedianCost()
        val totalGoals = 25.0

        assertTrue(kotlin.math.abs(result.first - 23.0 / totalGoals) < 0.0001)
        assertTrue(kotlin.math.abs(result.second - 2.0 / totalGoals) < 0.0001)
    }
}
