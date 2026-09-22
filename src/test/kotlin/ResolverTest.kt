import model.Player
import model.Position
import model.Team
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResolverTest {

    private fun player(
        name: String,
        team: Team,
        position: Position,
        nationality: String,
        agency: String?,
        cost: Long,
        goals: Int,
        assists: Int,
        red: Int = 0,
        yellow: Int = 0,
        matches: Int = 10
    ) = Player(
        name = name,
        team = team,
        position = position,
        nationality = nationality,
        agency = agency,
        transferCost = cost,
        participations = matches,
        goals = goals,
        assists = assists,
        yellowCards = yellow,
        redCards = red
    )

    private val teamA = Team("Team A", "City A")
    private val teamB = Team("Team B", "City B")

    private val players = listOf(
        player("Alice", teamA, Position.DEFENDER, "Germany", null, 100, 10, 1, red = 1),
        player("Bob", teamA, Position.FORWARD, "Germany", "Agency B", 300, 5, 10, red = 0),
        player("Carol", teamB, Position.GOALKEEPER, "France", "Agency A", 200, 2, 1, red = 3)
    )

    private val resolver = Resolver(players)

    @Test
    fun `counts players without agency`() {
        assertEquals(1, resolver.getCountWithoutAgency())
    }

    @Test
    fun `finds best defender scorer`() {
        assertEquals("Alice" to 10, resolver.getBestScorerDefender())
    }

    @Test
    fun `finds position of most expensive German player`() {
        assertEquals("нападающий", resolver.getTheExpensiveGermanPlayerPosition())
    }

    @Test
    fun `finds team with highest average red cards`() {
        assertEquals(teamB, resolver.getTheRudestTeam())
    }

    @Test
    fun `sorts average transfer costs descending`() {
        val result = resolver.getAverageTransferCostByPosition()
        assertEquals(
            listOf(Position.FORWARD, Position.GOALKEEPER, Position.DEFENDER),
            result.keys.toList()
        )
    }

    @Test
    fun `returns top three by usefulness`() {
        val result = resolver.getMostValuablePlayers()
        assertEquals(listOf("Bob", "Alice", "Carol"), result.map { it.name })
        assertEquals(listOf(25, 12, 4), result.map { it.goals + 2 * it.assists })
    }

    @Test
    fun `finds most popular agency by country`() {
        val result = resolver.getMostPopularAgencyByCountry()
        assertEquals("Agency B", result["Germany"])
        assertEquals("Agency A", result["France"])
    }

    @Test
    fun `calculates goal shares by median cost`() {
        // Costs: 100, 200, 300 => median 200.
        // Goals not above median: 10 + 2 = 12, all goals = 17.
        val result = resolver.getGoalsShareByMedianCost()
        assertTrue(result.first > 0.70 && result.first < 0.71)
        assertTrue(result.second > 0.29 && result.second < 0.30)
    }
}
