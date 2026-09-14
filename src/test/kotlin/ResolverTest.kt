import model.Player
import model.Position
import model.Team
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolverTest {

    private val reds = Team("Reds", "London")
    private val blues = Team("Blues", "Berlin")
    private val greens = Team("Greens", "Madrid")

    private val alice = player(
        name = "Alice",
        team = reds,
        position = Position.DEFENDER,
        country = "Germany",
        agency = "Alpha",
        cost = 100,
        goals = 5,
        assists = 1,
        redCards = 2
    )

    private val bob = player(
        name = "Bob",
        team = reds,
        position = Position.DEFENDER,
        country = "Brazil",
        agency = null,
        cost = 50,
        goals = 8,
        assists = 0,
        redCards = 0
    )

    private val clara = player(
        name = "Clara",
        team = blues,
        position = Position.FORWARD,
        country = "Germany",
        agency = "Beta",
        cost = 200,
        goals = 4,
        assists = 3,
        redCards = 3
    )

    private val david = player(
        name = "David",
        team = blues,
        position = Position.MIDFIELD,
        country = "Brazil",
        agency = "Alpha",
        cost = 70,
        goals = 2,
        assists = 4,
        redCards = 1
    )

    private val eva = player(
        name = "Eva",
        team = greens,
        position = Position.GOALKEEPER,
        country = "Spain",
        agency = null,
        cost = 60,
        goals = 0,
        assists = 0,
        redCards = 0
    )

    private val players = listOf(alice, bob, clara, david, eva)
    private val resolver = Resolver(players)

    @Test
    fun `counts players without agency`() {
        assertEquals(2, resolver.getCountWithoutAgency())
    }

    @Test
    fun `finds best defender scorer`() {
        assertEquals(
            "Bob" to 8,
            resolver.getBestScorerDefender()
        )
    }

    @Test
    fun `finds position of most expensive German player`() {
        assertEquals(
            "нападающий",
            resolver.getTheExpensiveGermanPlayerPosition()
        )
    }

    @Test
    fun `finds team with highest average red cards`() {
        assertEquals(blues, resolver.getTheRudestTeam())
    }

    @Test
    fun `calculates and sorts average costs by position`() {
        val result = resolver.getAverageTransferCostByPosition()

        assertEquals(
            listOf(
                Position.FORWARD,
                Position.DEFENDER,
                Position.MIDFIELD,
                Position.GOALKEEPER
            ),
            result.keys.toList()
        )

        assertEquals(200.0, result.getValue(Position.FORWARD))
        assertEquals(75.0, result.getValue(Position.DEFENDER))
    }

    @Test
    fun `finds top three valuable players with name tie breaker`() {
        assertEquals(
            listOf("Clara", "David", "Bob"),
            resolver.getMostValuablePlayers().map { it.name }
        )
    }

    @Test
    fun `finds most popular agency by country`() {
        assertEquals(
            mapOf(
                "Brazil" to "Alpha",
                "Germany" to "Alpha"
            ),
            resolver.getMostPopularAgencyByCountry()
        )
    }

    @Test
    fun `calculates goals shares by median cost`() {
        val result = resolver.getGoalsShareByMedianCost()

        assertEquals(10.0 / 19.0, result.first, 0.000001)
        assertEquals(9.0 / 19.0, result.second, 0.000001)
    }

    private fun player(
        name: String,
        team: Team,
        position: Position,
        country: String,
        agency: String?,
        cost: Long,
        goals: Int,
        assists: Int,
        redCards: Int
    ) = Player(
        name = name,
        team = team,
        position = position,
        country = country,
        agency = agency,
        transferValue = cost,
        appearances = 20,
        goals = goals,
        assists = assists,
        yellowCards = 0,
        redCards = redCards
    )
}