import model.Position
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolverTest {
    private val anna = player("Anna", "Reds", position = Position.DEFENDER, country = "Germany", agency = null,
        cost = 100, goals = 5, assists = 1, red = 2)
    private val boris = player("Boris", "Reds", position = Position.MIDFIELD, country = "Germany", agency = "Alpha",
        cost = 300, goals = 7, assists = 0, red = 0)
    private val clara = player("Clara", "Blues", city = "Paris", position = Position.FORWARD, country = "France",
        agency = "Alpha", cost = 200, goals = 3, assists = 4, red = 3)
    private val players = listOf(anna, boris, clara)
    private val resolver = Resolver(players)

    @Test
    fun `1 count without agency`() = assertEquals(1, resolver.getCountWithoutAgency())

    @Test
    fun `2 best scorer defender`() = assertEquals("Anna" to 5, resolver.getBestScorerDefender())

    @Test
    fun `3 position of most expensive german`() =
        assertEquals("полузащитник", resolver.getTheExpensiveGermanPlayerPosition())

    @Test
    fun `4 rudest team`() {
        // Reds: (2 + 0) / 2 = 1.0; Blues: 3 / 1 = 3.0
        val team = resolver.getTheRudestTeam()
        assertEquals("Blues", team.name)
        assertEquals(3.0, team.averageRedCards)
    }

    @Test
    fun `5 average cost by position is sorted descending`() {
        val result = resolver.getAverageTransferCostByPosition()
        assertEquals(listOf(Position.MIDFIELD, Position.FORWARD, Position.DEFENDER), result.keys.toList())
        assertEquals(300.0, result.getValue(Position.MIDFIELD))
        assertEquals(100.0, result.getValue(Position.DEFENDER))
    }

    @Test
    fun `6 most valuable players with alphabetical tie break`() {
        // Clara 3+8=11, Anna 5+2=7, Boris 7+0=7 (ничья -> Anna раньше)
        assertEquals(listOf("Clara", "Anna", "Boris"), resolver.getMostValuablePlayers().map { it.name })
    }

    @Test
    fun `6 returns only top 3`() {
        val many = players + player("Dan", goals = 1)
        assertEquals(3, Resolver(many).getMostValuablePlayers().size)
    }

    @Test
    fun `7 most popular agency by country`() {
        assertEquals(mapOf("Germany" to "Alpha", "France" to "Alpha"), resolver.getMostPopularAgencyByCountry())
    }

    @Test
    fun `7 tie is resolved alphabetically and null agencies are ignored`() {
        val set = listOf(
            player("A", country = "Spain", agency = "Zeta"),
            player("B", country = "Spain", agency = "Alpha"),
            player("C", country = "Spain", agency = null),
            player("D", country = "Italy", agency = "Beta"),
            player("E", country = "Italy", agency = "Beta"),
            player("F", country = "Italy", agency = "Alpha"),
        )
        assertEquals(mapOf("Spain" to "Alpha", "Italy" to "Beta"), Resolver(set).getMostPopularAgencyByCountry())
    }

    @Test
    fun `8 goals share by median cost`() {
        // медиана 200: не дороже -> Anna(5) + Clara(3) = 8 из 15
        val (cheap, expensive) = resolver.getGoalsShareByMedianCost()
        assertEquals(8.0 / 15, cheap, 1e-9)
        assertEquals(7.0 / 15, expensive, 1e-9)
    }

    @Test
    fun `8 median of even count is average of middle values`() {
        assertEquals(250.0, Resolver.medianOf(listOf(100L, 200L, 300L, 400L)))
    }

    @Test
    fun `8 shares sum to one on clean dataset`() {
        val all = parser.CsvParser.parseResource("fakePlayers.csv").players
        val (a, b) = Resolver(all).getGoalsShareByMedianCost()
        assertEquals(1.0, a + b, 1e-9)
    }
}
