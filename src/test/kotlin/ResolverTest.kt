import model.Player
import model.Position
import model.Team
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolverTest {

    private val teamA = Team("Alpha", "Moscow")
    private val teamB = Team("Beta", "SPb")
    private val teamC = Team("Gamma", "Kazan")

    private val players = listOf(
        Player("Alice", teamA, Position.FORWARD, "Germany", null, 10_000_000, 20, 10, 5, 1, 0),
        Player("Bob", teamA, Position.DEFENDER, "Germany", "AgencyX", 8_000_000, 25, 7, 2, 2, 3),
        Player("Carol", teamB, Position.DEFENDER, "Brazil", "AgencyY", 12_000_000, 18, 9, 1, 0, 1),
        Player("Dave", teamB, Position.MIDFIELD, "Brazil", "AgencyX", 6_000_000, 22, 4, 8, 3, 0),
        Player("Eve", teamC, Position.GOALKEEPER, "Germany", null, 15_000_000, 30, 0, 0, 1, 2),
        Player("Frank", teamC, Position.FORWARD, "Spain", "AgencyY", 20_000_000, 15, 15, 3, 0, 0)
    )

    private val resolver = Resolver(players)

    @Test
    fun `count without agency`() {
        // Alice и Eve
        assertEquals(2, resolver.getCountWithoutAgency())
    }

    @Test
    fun `best scorer among defenders`() {
        // Bob 7, Carol 9 -> Carol
        assertEquals("Carol" to 9, resolver.getBestScorerDefender())
    }

    @Test
    fun `expensive german player position`() {
        // немцы: Alice 10M FORWARD, Bob 8M DEFENDER, Eve 15M GOALKEEPER
        assertEquals("вратарь", resolver.getTheExpensiveGermanPlayerPosition())
    }

    @Test
    fun `rudest team by average red cards`() {
        // Alpha: Bob 3 -> avg 1.5 (Alice 0, Bob 3)
        // Beta: Carol 1, Dave 0 -> avg 0.5
        // Gamma: Eve 2, Frank 0 -> avg 1.0
        val rudest = resolver.getTheRudestTeam()
        assertEquals("Alpha", rudest.name)
    }

    @Test
    fun `average transfer cost by position sorted desc`() {
        val result = resolver.getAverageTransferCostByPosition()
        val positions = result.keys.toList()
        // FORWARD: (10M+20M)/2 = 15M
        // GOALKEEPER: 15M
        // DEFENDER: (8M+12M)/2 = 10M
        // MIDFIELD: 6M
        assertEquals(Position.FORWARD, positions[0])
        assertEquals(Position.GOALKEEPER, positions[1])
        assertEquals(Position.DEFENDER, positions[2])
        assertEquals(Position.MIDFIELD, positions[3])
        assertEquals(15_000_000.0, result[Position.FORWARD]!!)
        assertEquals(6_000_000.0, result[Position.MIDFIELD]!!)
    }

    @Test
    fun `most valuable players top 3`() {
        // полезность: goals + 2*assists
        // Alice: 10+10=20
        // Bob: 7+4=11
        // Carol: 9+2=11
        // Dave: 4+16=20
        // Eve: 0
        // Frank: 15+6=21
        // топ: Frank(21), Alice(20), Dave(20) — Alice раньше Dave по алфавиту
        val top = resolver.getMostValuablePlayers()
        assertEquals(listOf("Frank", "Alice", "Dave"), top.map { it.name })
    }

    @Test
    fun `most popular agency by country`() {
        val result = resolver.getMostPopularAgencyByCountry()
        // Germany: только Bob -> AgencyX (Alice/Eve без агентства)
        assertEquals("AgencyX", result["Germany"])
        // Brazil: Carol AgencyY, Dave AgencyX — по одному, алфавит -> AgencyX
        assertEquals("AgencyX", result["Brazil"])
        // Spain: Frank -> AgencyY
        assertEquals("AgencyY", result["Spain"])
    }

    @Test
    fun `goals share by median cost`() {
        // стоимости: 6, 8, 10, 12, 15, 20 -> медиана (10+12)/2 = 11
        // <=11: Alice 10, Bob 7, Dave 4 = 21
        // >11: Carol 9, Eve 0, Frank 15 = 24
        // всего 45
        val (cheap, expensive) = resolver.getGoalsShareByMedianCost()
        assertEquals(21.0 / 45.0, cheap, 1e-9)
        assertEquals(24.0 / 45.0, expensive, 1e-9)
    }
}
