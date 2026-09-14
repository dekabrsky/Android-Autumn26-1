import model.Position
import model.Team
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolverTest {

    private val alpha = Team("Alpha", "Boston")
    private val beta = Team("Beta", "Denver")

    private val bob = player(
        name = "Bob", team = alpha, position = Position.DEFENDER, nationality = "Germany", agency = "X",
        transferCost = 100, goals = 5, assists = 1, redCards = 2,
    )
    private val alice = player(
        name = "Alice", team = alpha, position = Position.FORWARD, nationality = "Germany", agency = null,
        transferCost = 300, goals = 10, assists = 0, redCards = 0,
    )
    private val carl = player(
        name = "Carl", team = beta, position = Position.DEFENDER, nationality = "Brazil", agency = "Y",
        transferCost = 200, goals = 7, assists = 3, redCards = 3,
    )
    private val dan = player(
        name = "Dan", team = beta, position = Position.GOALKEEPER, nationality = "Brazil", agency = "Y",
        transferCost = 50, goals = 0, assists = 6, redCards = 4,
    )

    private val resolver = Resolver(listOf(bob, alice, carl, dan))

    @Test
    fun `1 counts players without agency`() {
        assertEquals(1, resolver.getCountWithoutAgency())
    }

    @Test
    fun `2 finds best scorer among defenders`() {
        assertEquals("Carl" to 7, resolver.getBestScorerDefender())
    }

    @Test
    fun `3 returns russian position of the most expensive german`() {
        assertEquals("нападающий", resolver.getTheExpensiveGermanPlayerPosition())
    }

    @Test
    fun `4 finds team with highest average red cards`() {
        // Alpha: (2 + 0) / 2 = 1.0, Beta: (3 + 4) / 2 = 3.5
        assertEquals(beta, resolver.getTheRudestTeam())
    }

    @Test
    fun `4 compares average not total red cards`() {
        val resolver = Resolver(
            listOf(
                player("A1", team = alpha, redCards = 3),
                player("A2", team = alpha, redCards = 3),
                player("A3", team = alpha, redCards = 3),
                player("B1", team = beta, redCards = 5),
            )
        )
        assertEquals(beta, resolver.getTheRudestTeam())
    }

    @Test
    fun `5 averages transfer cost by position sorted descending`() {
        val result = resolver.getAverageTransferCostByPosition()
        assertEquals(listOf(Position.FORWARD, Position.DEFENDER, Position.GOALKEEPER), result.keys.toList())
        assertEquals(listOf(300.0, 150.0, 50.0), result.values.toList())
    }

    @Test
    fun `6 returns top 3 by goals plus doubled assists`() {
        // Bob 7, Alice 10, Carl 13, Dan 12
        assertEquals(listOf(carl, dan, alice), resolver.getMostValuablePlayers())
    }

    @Test
    fun `6 breaks usefulness tie by name`() {
        val zed = player("Zed", goals = 10)
        val amy = player("Amy", goals = 8, assists = 1)
        val max = player("Max", goals = 4)
        val kim = player("Kim", goals = 1)
        assertEquals(listOf(amy, zed, max), Resolver(listOf(zed, kim, max, amy)).getMostValuablePlayers())
    }

    @Test
    fun `7 finds most popular agency by country ignoring players without agency`() {
        assertEquals(mapOf("Brazil" to "Y", "Germany" to "X"), resolver.getMostPopularAgencyByCountry())
    }

    @Test
    fun `7 breaks agency tie alphabetically`() {
        val resolver = Resolver(
            listOf(
                player("P1", nationality = "Qatar", agency = "Walker and Sons"),
                player("P2", nationality = "Qatar", agency = "Fadel LLC"),
                player("P3", nationality = "Qatar", agency = null),
                player("P4", nationality = "Qatar", agency = null),
            )
        )
        assertEquals(mapOf("Qatar" to "Fadel LLC"), resolver.getMostPopularAgencyByCountry())
    }

    @Test
    fun `8 splits goals by median cost for even count`() {
        // Стоимости 50, 100, 200, 300 -> медиана 150. Не дороже: Bob (5) и Dan (0); дороже: Alice (10) и Carl (7).
        val (cheap, expensive) = resolver.getGoalsShareByMedianCost()
        assertEquals(5.0 / 22, cheap, 1e-9)
        assertEquals(17.0 / 22, expensive, 1e-9)
    }

    @Test
    fun `8 includes player priced exactly at median for odd count`() {
        val resolver = Resolver(
            listOf(
                player("Cheap", transferCost = 10, goals = 1),
                player("Median", transferCost = 20, goals = 2),
                player("Expensive", transferCost = 30, goals = 7),
            )
        )
        val (cheap, expensive) = resolver.getGoalsShareByMedianCost()
        assertEquals(0.3, cheap, 1e-9)
        assertEquals(0.7, expensive, 1e-9)
    }
}
