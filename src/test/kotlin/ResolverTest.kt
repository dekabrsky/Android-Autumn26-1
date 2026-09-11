import kotlin.test.Test
import kotlin.test.assertEquals
import model.Player
import model.Position
import model.Team
import resolver.Resolver

class ResolverTest {
    private fun player(
        name: String,
        team: Team,
        position: Position,
        nationality: String,
        agency: String?,
        cost: Int,
        goals: Int,
        assists: Int = 0,
        redCards: Int = 0,
    ) = Player(name, team, position, nationality, agency, cost, 10, goals, assists, 0, redCards)
        .also(team::addPlayer)

    private val alpha = Team("Alpha", "A-city")
    private val beta = Team("Beta", "B-city")

    @Test
    fun `counts players without agency`() {
        val resolver = Resolver(listOf(
            player("A", alpha, Position.FORWARD, "Germany", null, 10, 1),
            player("B", beta, Position.DEFENDER, "Brazil", "Agency", 20, 2),
        ))

        assertEquals(1, resolver.getCountWithoutAgency())
    }

    @Test
    fun `returns highest scoring defender`() {
        val resolver = Resolver(listOf(
            player("Low", alpha, Position.DEFENDER, "Brazil", "A", 10, 4),
            player("Zed", beta, Position.DEFENDER, "Brazil", "A", 10, 8),
            player("Adam", beta, Position.GOALKEEPER, "Brazil", "A", 10, 8),
            player("Forward", beta, Position.FORWARD, "Brazil", "A", 10, 20),
        ))

        assertEquals("Zed" to 8, resolver.getBestScorerDefender())
    }

    @Test
    fun `returns Russian position name of most expensive German`() {
        val resolver = Resolver(listOf(
            player("Cheap", alpha, Position.FORWARD, "Germany", "A", 10, 1),
            player("Expensive", beta, Position.GOALKEEPER, "Germany", "A", 30, 1),
        ))

        assertEquals("вратарь", resolver.getTheExpensiveGermanPlayerPosition())
    }

    @Test
    fun `returns team with highest average red cards`() {
        val resolver = Resolver(listOf(
            player("A1", alpha, Position.FORWARD, "Germany", "A", 10, 1, redCards = 2),
            player("A2", alpha, Position.FORWARD, "Germany", "A", 10, 1, redCards = 4),
            player("B1", beta, Position.FORWARD, "Germany", "A", 10, 1, redCards = 4),
            player("B2", beta, Position.FORWARD, "Germany", "A", 10, 1, redCards = 6),
        ))

        assertEquals(beta, resolver.getTheRudestTeam())
    }

    @Test
    fun `averages transfer cost by position in descending order`() {
        val resolver = Resolver(listOf(
            player("A", alpha, Position.FORWARD, "Germany", "A", 100, 1),
            player("B", alpha, Position.FORWARD, "Germany", "A", 300, 1),
            player("C", beta, Position.DEFENDER, "Brazil", "A", 50, 1),
        ))

        assertEquals(
            linkedMapOf(Position.FORWARD to 200.0, Position.DEFENDER to 50.0),
            resolver.getAverageTransferCostByPosition(),
        )
    }

    @Test
    fun `returns three most valuable players and sorts equal scores by name`() {
        val resolver = Resolver(listOf(
            player("Zoe", alpha, Position.FORWARD, "Germany", "A", 10, goals = 5),
            player("Anna", alpha, Position.FORWARD, "Germany", "A", 10, goals = 3, assists = 1),
            player("Bob", beta, Position.FORWARD, "Germany", "A", 10, goals = 1, assists = 1),
            player("Last", beta, Position.FORWARD, "Germany", "A", 10, goals = 0),
        ))

        assertEquals(listOf("Anna", "Zoe", "Bob"), resolver.getMostValuablePlayers().map { it.name })
    }

    @Test
    fun `returns most common agency per country with alphabetical tie break`() {
        val resolver = Resolver(listOf(
            player("A", alpha, Position.FORWARD, "Germany", "Bravo", 10, 1),
            player("B", alpha, Position.FORWARD, "Germany", "Alpha", 10, 1),
            player("C", beta, Position.FORWARD, "Brazil", "Solo", 10, 1),
            player("D", beta, Position.FORWARD, "Brazil", null, 10, 1),
        ))

        assertEquals(mapOf("Germany" to "Alpha", "Brazil" to "Solo"), resolver.getMostPopularAgencyByCountry())
    }

    @Test
    fun `splits goal share at transfer cost median`() {
        val resolver = Resolver(listOf(
            player("A", alpha, Position.FORWARD, "Germany", "A", 10, goals = 2),
            player("B", alpha, Position.FORWARD, "Germany", "A", 20, goals = 2),
            player("C", beta, Position.FORWARD, "Germany", "A", 30, goals = 6),
            player("D", beta, Position.FORWARD, "Germany", "A", 40, goals = 10),
        ))

        assertEquals(0.2 to 0.8, resolver.getGoalsShareByMedianCost())
    }

    @Test
    fun `splits goal share at median of odd player count`() {
        val resolver = Resolver(listOf(
            player("A", alpha, Position.FORWARD, "Germany", "A", 10, goals = 1),
            player("B", alpha, Position.FORWARD, "Germany", "A", 20, goals = 1),
            player("C", beta, Position.FORWARD, "Germany", "A", 30, goals = 2),
        ))

        assertEquals(0.5 to 0.5, resolver.getGoalsShareByMedianCost())
    }
}
