import model.Player
import model.Position
import model.Team
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolverTest {

    private val teamA = Team("Team A", "City A")
    private val teamB = Team("Team B", "City B")

    private val players = listOf(
        Player(
            name = "Alice",
            team = teamA,
            position = Position.DEFENDER,
            country = "Russia",
            agency = null,
            transferCost = 10_000_000,
            participations = 10,
            goals = 10,
            assists = 2,
            yellowCards = 1,
            redCards = 1
        ),
        Player(
            name = "Bob",
            team = teamA,
            position = Position.DEFENDER,
            country = "Germany",
            agency = "Alpha",
            transferCost = 20_000_000,
            participations = 15,
            goals = 15,
            assists = 5,
            yellowCards = 2,
            redCards = 3
        ),
        Player(
            name = "Carol",
            team = teamB,
            position = Position.FORWARD,
            country = "Germany",
            agency = "Beta",
            transferCost = 30_000_000,
            participations = 20,
            goals = 8,
            assists = 1,
            yellowCards = 3,
            redCards = 4
        ),
        Player(
            name = "Dave",
            team = teamB,
            position = Position.MIDFIELD,
            country = "Spain",
            agency = "Alpha",
            transferCost = 40_000_000,
            participations = 12,
            goals = 5,
            assists = 10,
            yellowCards = 1,
            redCards = 2
        )
    )

    private val resolver = Resolver(players)

    @Test
    fun `count players without agency`() {
        assertEquals(
            1,
            resolver.getCountWithoutAgency()
        )
    }

    @Test
    fun `find best scoring defender`() {
        assertEquals(
            "Bob" to 15,
            resolver.getBestScorerDefender()
        )
    }

    @Test
    fun `find position of most expensive German player`() {
        assertEquals(
            "нападающий",
            resolver.getTheExpensiveGermanPlayerPosition()
        )
    }

    @Test
    fun `find rudest team`() {
        assertEquals(
            teamB,
            resolver.getTheRudestTeam()
        )
    }

    @Test
    fun `calculate average transfer cost by position`() {
        val result = resolver.getAverageTransferCostByPosition()

        assertEquals(
            listOf(
                Position.MIDFIELD,
                Position.FORWARD,
                Position.DEFENDER
            ),
            result.keys.toList()
        )

        assertEquals(
            40_000_000.0,
            result[Position.MIDFIELD]
        )

        assertEquals(
            30_000_000.0,
            result[Position.FORWARD]
        )

        assertEquals(
            15_000_000.0,
            result[Position.DEFENDER]
        )
    }

    @Test
    fun `find top three most valuable players`() {
        val result = resolver.getMostValuablePlayers()

        assertEquals(
            listOf("Bob", "Dave", "Alice"),
            result.map { it.name }
        )
    }

    @Test
    fun `find most popular agency by country`() {
        val result = resolver.getMostPopularAgencyByCountry()

        assertEquals(
            "Alpha",
            result["Germany"]
        )

        assertEquals(
            "Alpha",
            result["Spain"]
        )
    }

    @Test
    fun `calculate goals share by median transfer cost`() {
        val result = resolver.getGoalsShareByMedianCost()

        assertEquals(
            25.0 / 38.0,
            result.first,
            0.000001
        )

        assertEquals(
            13.0 / 38.0,
            result.second,
            0.000001
        )
    }
}