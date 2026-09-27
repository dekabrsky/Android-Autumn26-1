import model.Player
import model.Position
import model.Team
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolverTest {

    private fun createPlayer(
        name: String = "Test",
        team: Team = Team("Team", "City"),
        position: Position = Position.FORWARD,
        nationality: String = "USA",
        agency: String? = null,
        cost: Long = 0,
        goals: Int = 0,
        assists: Int = 0,
        yellow: Int = 0,
        red: Int = 0
    ) = Player(name, team, position, nationality, agency, cost, 10, goals, assists, yellow, red)

    @Test
    fun `getCountWithoutAgency works`() {
        val players = listOf(
            createPlayer(agency = "Agent"),
            createPlayer(agency = null),
            createPlayer(agency = null)
        )
        val resolver = Resolver(players)
        assertEquals(2, resolver.getCountWithoutAgency())
    }

    @Test
    fun `getBestScorerDefender works`() {
        val players = listOf(
            createPlayer(name = "Def1", position = Position.DEFENDER, goals = 5),
            createPlayer(name = "Def2", position = Position.DEFENDER, goals = 10),
            createPlayer(name = "Fwd", position = Position.FORWARD, goals = 20)
        )
        val resolver = Resolver(players)
        val result = resolver.getBestScorerDefender()
        assertEquals("Def2", result.first)
        assertEquals(10, result.second)
    }

    @Test
    fun `getTheExpensiveGermanPlayerPosition works`() {
        val players = listOf(
            createPlayer(nationality = "Germany", cost = 1000, position = Position.MIDFIELD),
            createPlayer(nationality = "Germany", cost = 5000, position = Position.FORWARD),
            createPlayer(nationality = "USA", cost = 10000, position = Position.DEFENDER)
        )
        val resolver = Resolver(players)
        assertEquals("нападающий", resolver.getTheExpensiveGermanPlayerPosition())
    }

    @Test
    fun `getTheRudestTeam works`() {
        val t1 = Team("T1", "C1")
        val t2 = Team("T2", "C2")
        val players = listOf(
            createPlayer(team = t1, red = 2),
            createPlayer(team = t1, red = 0),
            createPlayer(team = t2, red = 3)
        )

        val resolver = Resolver(players)
        assertEquals(t2, resolver.getTheRudestTeam())
    }

    @Test
    fun `getAverageTransferCostByPosition works and is sorted`() {
        val players = listOf(
            createPlayer(position = Position.FORWARD, cost = 1000),
            createPlayer(position = Position.FORWARD, cost = 2000),
            createPlayer(position = Position.DEFENDER, cost = 5000)
        )
        val resolver = Resolver(players)
        val result = resolver.getAverageTransferCostByPosition()

        assertEquals(5000.0, result[Position.DEFENDER])
        assertEquals(1500.0, result[Position.FORWARD])

        val keys = result.keys.toList()
        assertEquals(Position.DEFENDER, keys[0])
        assertEquals(Position.FORWARD, keys[1])
    }

    @Test
    fun `getMostValuablePlayers works`() {
        val p1 = createPlayer(name = "B", goals = 10, assists = 0)
        val p2 = createPlayer(name = "A", goals = 10, assists = 0)
        val p3 = createPlayer(name = "C", goals = 5, assists = 5)
        val p4 = createPlayer(name = "D", goals = 1, assists = 1)

        val resolver = Resolver(listOf(p1, p2, p3, p4))
        val result = resolver.getMostValuablePlayers()

        assertEquals(3, result.size)
        assertEquals(p3, result[0])
        assertEquals(p2, result[1])
        assertEquals(p1, result[2])
    }

    @Test
    fun `getMostPopularAgencyByCountry works`() {
        val players = listOf(
            createPlayer(nationality = "USA", agency = "A1"),
            createPlayer(nationality = "USA", agency = "A1"),
            createPlayer(nationality = "USA", agency = "A2"),
            createPlayer(nationality = "UK", agency = "B2"),
            createPlayer(nationality = "UK", agency = "B1")
        )
        val resolver = Resolver(players)
        val result = resolver.getMostPopularAgencyByCountry()

        assertEquals("A1", result["USA"])
        assertEquals("B1", result["UK"])
    }

    @Test
    fun `getGoalsShareByMedianCost works`() {
        val players = listOf(
            createPlayer(cost = 10, goals = 1),
            createPlayer(cost = 20, goals = 2),
            createPlayer(cost = 30, goals = 3),
            createPlayer(cost = 40, goals = 4)
        )

        val resolver = Resolver(players)
        val result = resolver.getGoalsShareByMedianCost()
        assertEquals(0.3, result.first)
        assertEquals(0.7, result.second)
    }
}
