import model.Player
import model.Position
import model.Team
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolverTest {
    private val p1 = Player(
        "Alice", Team("A", "X"), Position.FORWARD, "Germany", null,
        100, 10, 5, 3, 1, 0
    )
    private val p2 = Player("Bob",Team("B","Y"), Position.DEFENDER, "France", "Ag1",
        200, 20, 7, 0, 2, 1)
    private val p3 = Player("Cara",Team("A","X"), Position.DEFENDER, "Germany", "Ag2",
        300, 30, 2, 5, 0, 0)
    private val players = listOf(p1, p2, p3)
    private val r = Resolver(players)

    @Test
    fun `playersWithoutAgency`() = assertEquals(1, r.getCountWithoutAgency())

    @Test fun `topScoringDefender`() = assertEquals("Bob" to 7, r.getBestScorerDefender())

    @Test fun `mostExpensiveGermanPosition`() =
        assertEquals("защитник", r.getTheExpensiveGermanPlayerPosition())

    @Test fun `teamWithMostRedCards`() = assertEquals(Team("B", "Y"), r.getTheRudestTeam())

    @Test fun `top3MostUseful order`() {
        // useful: p3=2+10=12, p2=7, p1=5+6=11 → p3, p1, p2
        assertEquals(listOf("Cara","Alice","Bob"), r.getMostValuablePlayers().map { it.Name })
    }

    @Test fun `popularAgencyByCountry`() {
        val map = r.getMostPopularAgencyByCountry()
        assertEquals("Ag2", map["Germany"])   // Ag2 > Ag1 по алфавиту при равенстве count=1
        assertEquals("Ag1", map["France"])
    }
}