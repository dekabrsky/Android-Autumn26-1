import model.Player
import model.Position
import model.Team
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolverTest {

    private val teamA = Team(
        name = "Alpha FC",
        city = "City A"
    )

    private val teamB = Team(
        name = "Beta FC",
        city = "City B"
    )

    private fun createPlayers(): List<Player> {
        return listOf(

            Player(
                name = "Anna",
                team = teamA,
                position = Position.DEFENDER,
                country = "Germany",
                agency = "Beta",
                transferCost = 100,
                participations = 10,
                goals = 5,
                assists = 1,
                yellowCards = 0,
                redCards = 1
            ),

            Player(
                name = "Boris",
                team = teamA,
                position = Position.DEFENDER,
                country = "Germany",
                agency = null,
                transferCost = 300,
                participations = 10,
                goals = 8,
                assists = 0,
                yellowCards = 2,
                redCards = 3
            ),

            Player(
                name = "Clara",
                team = teamB,
                position = Position.FORWARD,
                country = "Brazil",
                agency = "Alpha",
                transferCost = 250,
                participations = 10,
                goals = 6,
                assists = 3,
                yellowCards = 1,
                redCards = 0
            ),

            Player(
                name = "Dmitry",
                team = teamB,
                position = Position.MIDFIELD,
                country = "Brazil",
                agency = "Beta",
                transferCost = 400,
                participations = 10,
                goals = 2,
                assists = 5,
                yellowCards = 0,
                redCards = 2
            )
        )
    }

    @Test
    fun `query 1 counts players without agency`() {
        val resolver = Resolver(createPlayers())

        assertEquals(
            1,
            resolver.getCountWithoutAgency()
        )
    }

    @Test
    fun `query 2 finds defender with most goals`() {
        val resolver = Resolver(createPlayers())

        assertEquals(
            Pair("Boris", 8),
            resolver.getBestScorerDefender()
        )
    }

    @Test
    fun `query 3 finds position of most expensive German player`() {
        val resolver = Resolver(createPlayers())

        assertEquals(
            "защитник",
            resolver.getTheExpensiveGermanPlayerPosition()
        )
    }

    @Test
    fun `query 4 finds team with highest average red cards`() {
        val resolver = Resolver(createPlayers())

        assertEquals(
            teamA,
            resolver.getTheRudestTeam()
        )
    }

    @Test
    fun `query 5 calculates average transfer cost by position`() {
        val resolver = Resolver(createPlayers())

        val result =
            resolver.getAverageTransferCostByPosition()

        assertEquals(
            listOf(
                Position.MIDFIELD,
                Position.FORWARD,
                Position.DEFENDER
            ),
            result.keys.toList()
        )

        assertEquals(
            400.0,
            result[Position.MIDFIELD]!!,
            0.001
        )

        assertEquals(
            250.0,
            result[Position.FORWARD]!!,
            0.001
        )

        assertEquals(
            200.0,
            result[Position.DEFENDER]!!,
            0.001
        )
    }

    @Test
    fun `query 6 returns top three valuable players`() {
        val resolver = Resolver(createPlayers())

        val result =
            resolver.getMostValuablePlayers()

        assertEquals(
            listOf(
                "Clara",
                "Dmitry",
                "Boris"
            ),
            result.map { it.name }
        )
    }

    @Test
    fun `query 7 finds most popular agency by country`() {
        val resolver = Resolver(createPlayers())

        val result =
            resolver.getMostPopularAgencyByCountry()

        assertEquals(
            "Beta",
            result["Germany"]
        )

        assertEquals(
            "Alpha",
            result["Brazil"]
        )
    }

    @Test
    fun `query 8 calculates goal shares around median cost`() {
        val resolver = Resolver(createPlayers())

        val result =
            resolver.getGoalsShareByMedianCost()

        assertEquals(
            11.0 / 21.0,
            result.first,
            0.000001
        )

        assertEquals(
            10.0 / 21.0,
            result.second,
            0.000001
        )
    }
}