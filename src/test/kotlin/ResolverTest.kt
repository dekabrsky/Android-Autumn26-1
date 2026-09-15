import model.Player
import model.Position
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolverTest {

    private val teamAlpha = "Nevada whales"
    private val countryDe = "Germany"

    private fun createTestData(): List<Player> {
        val playerA = Player(
            name = "Anna Schmidt",
            teamName = teamAlpha,
            city = "South Carolina",
            position = Position.DEFENDER,
            nationality = countryDe,
            agency = "Agency A",
            transferCost = 5_000_000,
            participations = 20,
            goals = 3,
            assists = 1,
            yellowCards = 2,
            redCards = 1,
        )

        val playerB = Player(
            name = "Boris Muller",
            teamName = teamAlpha,
            city = "South Carolina",
            position = Position.DEFENDER,
            nationality = countryDe,
            agency = "Agency A",
            transferCost = 9_000_000,
            participations = 25,
            goals = 5,
            assists = 0,
            yellowCards = 1,
            redCards = 3,
        )

        val playerC = Player(
            name = "Carlos Lima",
            teamName = "Hawaii oxen",
            city = "Oklahoma",
            position = Position.FORWARD,
            nationality = "Brazil",
            agency = null,
            transferCost = 3_000_000,
            participations = 15,
            goals = 10,
            assists = 4,
            yellowCards = 0,
            redCards = 0,
        )

        return listOf(playerA, playerB, playerC)
    }

    private val resolverInstance = Resolver(createTestData())

    @Test
    fun `should correctly calculate total players missing agency`() {
        val unassignedCount = resolverInstance.getCountWithoutAgency()
        assertEquals(1, unassignedCount)
    }

    @Test
    fun `should determine top scoring defender and goal count`() {
        val topDefender = resolverInstance.getBestScorerDefender()
        assertEquals("Boris Muller" to 5, topDefender)
    }

    @Test
    fun `should return position description for highest value German athlete`() {
        val expectedRole = "защитник"
        val actualRole = resolverInstance.getTheExpensiveGermanPlayerPosition()
        assertEquals(expectedRole, actualRole)
    }

    @Test
    fun `should identify club with highest red card average`() {
        val highestPenaltyTeam = resolverInstance.getTheRudestTeam()
        assertEquals(teamAlpha, highestPenaltyTeam.name)
    }

    @Test
    fun `should group average cost by position in descending order`() {
        val priceMap = resolverInstance.getAverageTransferCostByPosition()

        assertEquals(listOf(Position.DEFENDER, Position.FORWARD), priceMap.keys.toList())
        assertEquals(7_000_000.0, priceMap[Position.DEFENDER])
        assertEquals(3_000_000.0, priceMap[Position.FORWARD])
    }

    @Test
    fun `should rank top 3 players by impact score followed by name`() {
        val rankedPlayerNames = resolverInstance.getMostValuablePlayers().map { it.name }
        val expectedOrder = listOf("Carlos Lima", "Anna Schmidt", "Boris Muller")

        assertEquals(expectedOrder, rankedPlayerNames)
    }

    @Test
    fun `should compute goal share split by median cost threshold`() {
        val (lowerOrEqualShare, strictlyAboveShare) = resolverInstance.getGoalsShareByMedianCost()

        assertEquals(13.0 / 18.0, lowerOrEqualShare)
        assertEquals(5.0 / 18.0, strictlyAboveShare)
    }
}