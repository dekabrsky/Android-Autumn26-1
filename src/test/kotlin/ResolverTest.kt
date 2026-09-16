import model.Player
import model.Position
import model.Team
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolverTest {

    private val firstTeam = Team(
        name = "North Stars",
        city = "Kazan"
    )

    private val secondTeam = Team(
        name = "South United",
        city = "Sochi"
    )

    private fun testPlayers(): List<Player> {
        return listOf(
            Player(
                name = "Anton",
                team = firstTeam,
                position = Position.DEFENDER,
                country = "Germany",
                agency = "Agency B",
                transferCost = 120,
                participations = 12,
                goals = 4,
                assists = 1,
                yellowCards = 0,
                redCards = 1
            ),

            Player(
                name = "Boris",
                team = firstTeam,
                position = Position.DEFENDER,
                country = "Germany",
                agency = null,
                transferCost = 320,
                participations = 14,
                goals = 8,
                assists = 0,
                yellowCards = 2,
                redCards = 3
            ),

            Player(
                name = "Chris",
                team = secondTeam,
                position = Position.FORWARD,
                country = "Brazil",
                agency = "Agency A",
                transferCost = 260,
                participations = 11,
                goals = 6,
                assists = 3,
                yellowCards = 1,
                redCards = 0
            ),

            Player(
                name = "Denis",
                team = secondTeam,
                position = Position.MIDFIELD,
                country = "Brazil",
                agency = "Agency B",
                transferCost = 420,
                participations = 13,
                goals = 2,
                assists = 5,
                yellowCards = 0,
                redCards = 2
            )
        )
    }

    @Test
    fun `counts players without agency`() {
        val resolver = Resolver(testPlayers())

        assertEquals(
            1,
            resolver.getCountWithoutAgency()
        )
    }

    @Test
    fun `finds best scoring defender`() {
        val resolver = Resolver(testPlayers())

        assertEquals(
            "Boris" to 8,
            resolver.getBestScorerDefender()
        )
    }

    @Test
    fun `finds position of most expensive German player`() {
        val resolver = Resolver(testPlayers())

        assertEquals(
            "защитник",
            resolver.getTheExpensiveGermanPlayerPosition()
        )
    }

    @Test
    fun `finds team with highest average red cards`() {
        val resolver = Resolver(testPlayers())

        assertEquals(
            firstTeam,
            resolver.getTheRudestTeam()
        )
    }

    @Test
    fun `calculates average transfer cost for positions`() {
        val resolver = Resolver(testPlayers())

        val result =
            resolver.getAverageTransferCostByPosition()

        assertEquals(
            420.0,
            result[Position.MIDFIELD]!!,
            0.001
        )

        assertEquals(
            260.0,
            result[Position.FORWARD]!!,
            0.001
        )

        assertEquals(
            220.0,
            result[Position.DEFENDER]!!,
            0.001
        )
    }

    @Test
    fun `returns three most valuable players`() {
        val resolver = Resolver(testPlayers())

        val names = resolver
            .getMostValuablePlayers()
            .map { it.name }

        assertEquals(
            listOf(
                "Chris",
                "Denis",
                "Boris"
            ),
            names
        )
    }

    @Test
    fun `selects most popular agency for each country`() {
        val resolver = Resolver(testPlayers())

        val result =
            resolver.getMostPopularAgencyByCountry()

        assertEquals(
            "Agency B",
            result["Germany"]
        )

        assertEquals(
            "Agency A",
            result["Brazil"]
        )
    }

    @Test
    fun `calculates goal shares around median cost`() {
        val resolver = Resolver(testPlayers())

        val result =
            resolver.getGoalsShareByMedianCost()

        assertEquals(
            10.0 / 20.0,
            result.first,
            0.000001
        )

        assertEquals(
            10.0 / 20.0,
            result.second,
            0.000001
        )
    }
}