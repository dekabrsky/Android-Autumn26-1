import model.Player
import model.Position
import model.Team
import resolver.Resolver
import kotlin.test.*

class ResolverTest {
    private val a = Player("Alex", Team("A", "City"), Position.DEFENDER, "Germany", null, 10, 25, 2, 3, 0, 1)
    private val b = a.copy(name = "Bob", position = Position.FORWARD, agency = "Beta", transferCost = 30, goals = 6, assists = 1, redCards = 3)
    private val c = a.copy(name = "Carl", team = Team("B", "City"), agency = "Alpha", transferCost = 20, goals = 4, assists = 0, redCards = 4)
    private val resolver = Resolver(listOf(c, b, a))

    @Test fun `counts missing agencies`() { assertEquals(1, resolver.getCountWithoutAgency()) }
    @Test fun `best defender includes name and goals`() { assertEquals("Carl" to 4, resolver.getBestScorerDefender()) }
    @Test fun `most expensive German position is Russian`() { assertEquals("нападающий", resolver.getTheExpensiveGermanPlayerPosition()) }
    @Test fun `rudest team uses average and retains roster`() {
        val team = resolver.getTheRudestTeam()
        assertEquals(Team("B", "City"), team)
        assertEquals(listOf(c), team.players)
        assertEquals(4.0, team.averageRedCards)
    }
    @Test fun `cost averages sorted descending`() {
        val averages = resolver.getAverageTransferCostByPosition()
        assertEquals(listOf(Position.FORWARD, Position.DEFENDER), averages.keys.toList())
        assertEquals(listOf(30.0, 15.0), averages.values.toList())
    }
    @Test fun `usefulness sorts ties by name and limits to three`() {
        val fourth = c.copy(name = "Dan", goals = 0)
        assertEquals(listOf("Alex", "Bob", "Carl"), Resolver(listOf(fourth, c, b, a)).getMostValuablePlayers().map { it.name })
    }
    @Test fun `agency ties use alphabet and absent agencies are excluded`() {
        assertEquals(mapOf("Germany" to "Alpha"), resolver.getMostPopularAgencyByCountry())
        assertEquals(emptyMap(), Resolver(listOf(a)).getMostPopularAgencyByCountry())
        assertEquals(mapOf("Germany" to "Beta"), Resolver(listOf(b, b.copy(name = "Ben"), c)).getMostPopularAgencyByCountry())
    }
    @Test fun `median includes equal cost in cheaper half`() {
        assertEquals(20.0, resolver.getMedianTransferCost())
        assertEquals(0.5 to 0.5, resolver.getGoalsShareByMedianCost())
        val even = Resolver(listOf(a, b))
        assertEquals(20.0, even.getMedianTransferCost())
        assertEquals(0.25 to 0.75, even.getGoalsShareByMedianCost())
        assertEquals(1.0 to 0.0, Resolver(listOf(a, b.copy(transferCost = 10))).getGoalsShareByMedianCost())
    }
    @Test fun `empty selections and zero goals have explicit behavior`() {
        val empty = Resolver(emptyList())
        assertEquals(0, empty.getCountWithoutAgency())
        assertEquals(emptyMap(), empty.getAverageTransferCostByPosition())
        assertEquals(emptyMap(), empty.getMostPopularAgencyByCountry())
        assertEquals(emptyList(), empty.getMostValuablePlayers())
        assertEquals(0.0 to 0.0, empty.getGoalsShareByMedianCost())
        assertEquals(0.0 to 0.0, Resolver(listOf(a.copy(goals = 0))).getGoalsShareByMedianCost())
        assertFailsWith<NoSuchElementException> { empty.getBestScorerDefender() }
        assertFailsWith<NoSuchElementException> { empty.getTheExpensiveGermanPlayerPosition() }
        assertFailsWith<NoSuchElementException> { empty.getTheRudestTeam() }
        assertEquals("Alex" to 2, Resolver(listOf(a.copy(name = "Zed"), a)).getBestScorerDefender())
    }
    @Test fun `countries and team cities remain distinct`() {
        val other = b.copy(country = "Brazil", team = Team("A", "Other city"))
        val result = Resolver(listOf(a, other))
        assertEquals("защитник", result.getTheExpensiveGermanPlayerPosition())
        assertEquals(2, result.teams.size)
        assertEquals(mapOf("Brazil" to "Beta"), result.getMostPopularAgencyByCountry())
    }
}
