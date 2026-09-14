import model.Player
import model.Position
import model.Team
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolverTest {

    private val teamA = Team("Alpha", "CityA")
    private val teamB = Team("Beta", "CityB")

    // Стоимости: 5, 10, 20, 40 → медиана (10+20)/2 = 15
    private val players = listOf(
        Player("Ann", teamA, Position.DEFENDER, "Germany", null,       5_000_000, 10,  3, 2, 1, 0),
        Player("Bob", teamA, Position.FORWARD,  "Germany", "AgencyX", 10_000_000, 20,  5, 4, 2, 1),
        Player("Cid", teamB, Position.FORWARD,  "Brazil",  "AgencyY", 20_000_000, 25,  8, 1, 0, 3),
        Player("Dan", teamB, Position.DEFENDER, "Brazil",  "AgencyX", 40_000_000, 30, 10, 0, 1, 1)
    )

    private val resolver = Resolver(players)

    @Test fun `count without agency`() {
        assertEquals(1, resolver.getCountWithoutAgency())
    }

    @Test fun `best defender scorer`() {
        val (name, goals) = resolver.getBestScorerDefender()
        assertEquals("Dan", name)
        assertEquals(10, goals)
    }

    @Test fun `most expensive German position`() {
        // Немцы: Ann (5M, DEFENDER), Bob (10M, FORWARD) → Bob → нападающий
        assertEquals("нападающий", resolver.getTheExpensiveGermanPlayerPosition())
    }

    @Test fun `rudest team`() {
        // Alpha: (0 + 1) / 2 = 0.5; Beta: (3 + 1) / 2 = 2.0 → Beta
        assertEquals(teamB, resolver.getTheRudestTeam())
    }

    @Test
    fun `average cost by position sorted desc`() {
        // DEFENDER: (5 000 000 + 40 000 000) / 2 = 22 500 000
        // FORWARD:  (10 000 000 + 20 000 000) / 2 = 15 000 000
        val m = resolver.getAverageTransferCostByPosition()
        assertEquals(listOf(Position.DEFENDER, Position.FORWARD), m.keys.toList())
        assertEquals(22_500_000.0, m[Position.DEFENDER])
        assertEquals(15_000_000.0, m[Position.FORWARD])
    }

    @Test fun `top3 valuable`() {
        // Ann: 3+4=7;  Bob: 5+8=13;  Cid: 8+2=10;  Dan: 10+0=10
        // Cid и Dan делят 10 → по алфавиту Cid < Dan
        val top = resolver.getMostValuablePlayers()
        assertEquals(listOf("Bob", "Cid", "Dan"), top.map { it.name })
    }

    @Test fun `popular agency by country`() {
        // Германия: Bob → AgencyX (Ann null, пропущен)
        // Бразилия: Cid(AgencyY) и Dan(AgencyX) — 1:1 → по алфавиту AgencyX
        val m = resolver.getMostPopularAgencyByCountry()
        assertEquals("AgencyX", m["Germany"])
        assertEquals("AgencyX", m["Brazil"])
    }

    @Test fun `median goals split`() {
        // Стоимости отсортированы: 5, 10, 20, 40 → медиана (10 + 20)/2 = 15
        // ≤15M: Ann (3 гола) + Bob (5) = 8
        //  >15M: Cid (8) + Dan (10) = 18
        // всего: 26
        val (below, above) = resolver.getGoalsShareByMedianCost()
        assertEquals(8.0 / 26.0,  below, 1e-9)
        assertEquals(18.0 / 26.0, above, 1e-9)
    }
}