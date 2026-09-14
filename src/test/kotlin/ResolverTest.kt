import model.Player
import model.Position
import model.Team
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolverTest {

    private val lions = Team("Lions", "Berlin")
    private val bears = Team("Bears", "Munich")
    private val wolves = Team("Wolves", "Hamburg")

    private fun player(
        name: String,
        team: Team = lions,
        position: Position = Position.FORWARD,
        nationality: String = "Germany",
        agency: String? = "Alpha",
        cost: Long = 1_000,
        participations: Int = 10,
        goals: Int = 0,
        assists: Int = 0,
        yellow: Int = 0,
        red: Int = 0,
    ) = Player(name, team, position, nationality, agency, cost, participations, goals, assists, yellow, red)

    @Test
    fun `1 counts players without agency`() {
        val resolver = Resolver(
            listOf(
                player("A", agency = null),
                player("B", agency = "Alpha"),
                player("C", agency = null),
            ),
        )
        assertEquals(2, resolver.getCountWithoutAgency())
    }

    @Test
    fun `2 finds best scoring defender`() {
        val resolver = Resolver(
            listOf(
                player("Striker", position = Position.FORWARD, goals = 30),
                player("Wall", position = Position.DEFENDER, goals = 5),
                player("Rock", position = Position.DEFENDER, goals = 8),
            ),
        )
        assertEquals("Rock" to 8, resolver.getBestScorerDefender())
    }

    @Test
    fun `3 returns russian position of the most expensive german player`() {
        val resolver = Resolver(
            listOf(
                player("Hans", nationality = "Germany", position = Position.GOALKEEPER, cost = 500),
                player("Klaus", nationality = "Germany", position = Position.DEFENDER, cost = 900),
                player("Pedro", nationality = "Brazil", position = Position.FORWARD, cost = 5_000),
            ),
        )
        assertEquals("защитник", resolver.getTheExpensiveGermanPlayerPosition())
    }

    @Test
    fun `4 picks team with highest average red cards per player`() {
        val resolver = Resolver(
            listOf(
                player("A", team = lions, red = 10),
                player("B", team = lions, red = 0),   // Lions: avg 5
                player("C", team = bears, red = 6),   // Bears: avg 6
                player("D", team = wolves, red = 4),
                player("E", team = wolves, red = 4),  // Wolves: avg 4
            ),
        )
        assertEquals(bears, resolver.getTheRudestTeam())
    }

    @Test
    fun `5 averages cost by position sorted descending`() {
        val resolver = Resolver(
            listOf(
                player("A", position = Position.GOALKEEPER, cost = 100),
                player("B", position = Position.GOALKEEPER, cost = 300),  // avg 200
                player("C", position = Position.FORWARD, cost = 1_000),   // avg 1000
                player("D", position = Position.DEFENDER, cost = 400),
                player("E", position = Position.DEFENDER, cost = 600),    // avg 500
            ),
        )
        val result = resolver.getAverageTransferCostByPosition()
        assertEquals(listOf(Position.FORWARD, Position.DEFENDER, Position.GOALKEEPER), result.keys.toList())
        assertEquals(1000.0, result[Position.FORWARD])
        assertEquals(500.0, result[Position.DEFENDER])
        assertEquals(200.0, result[Position.GOALKEEPER])
    }

    @Test
    fun `6 returns top-3 by usefulness with alphabetical tie-break`() {
        val resolver = Resolver(
            listOf(
                player("Zed", goals = 4, assists = 3),   // 10
                player("Amy", goals = 10, assists = 0),  // 10
                player("Bob", goals = 1, assists = 6),   // 13
                player("Cal", goals = 2, assists = 1),   // 4
            ),
        )
        assertEquals(listOf("Bob", "Amy", "Zed"), resolver.getMostValuablePlayers().map { it.name })
    }

    @Test
    fun `7 finds most popular agency per country with alphabetical tie-break`() {
        val resolver = Resolver(
            listOf(
                player("A", nationality = "Germany", agency = "Beta"),
                player("B", nationality = "Germany", agency = "Beta"),
                player("C", nationality = "Germany", agency = "Alpha"),
                player("D", nationality = "Brazil", agency = "Zeta"),
                player("E", nationality = "Brazil", agency = "Gamma"),
                player("F", nationality = "Brazil", agency = null),
            ),
        )
        assertEquals(
            mapOf("Germany" to "Beta", "Brazil" to "Gamma"),
            resolver.getMostPopularAgencyByCountry(),
        )
    }

    @Test
    fun `8 splits goals share by median cost`() {
        val resolver = Resolver(
            listOf(
                player("A", cost = 100, goals = 1),
                player("B", cost = 200, goals = 2),
                player("C", cost = 300, goals = 3),   // median = 300, cheap goals = 6
                player("D", cost = 400, goals = 4),
                player("E", cost = 500, goals = 10),  // expensive goals = 14
            ),
        )
        assertEquals(300.0, resolver.medianTransferCost())
        val (cheap, expensive) = resolver.getGoalsShareByMedianCost()
        assertEquals(0.3, cheap, 1e-9)
        assertEquals(0.7, expensive, 1e-9)
    }

    @Test
    fun `8 median for even count is mean of two middle values`() {
        val resolver = Resolver(
            listOf(
                player("A", cost = 100, goals = 1),
                player("B", cost = 200, goals = 1),
                player("C", cost = 400, goals = 1),
                player("D", cost = 800, goals = 1),
            ),
        )
        assertEquals(300.0, resolver.medianTransferCost())
        assertEquals(0.5 to 0.5, resolver.getGoalsShareByMedianCost())
    }
}
