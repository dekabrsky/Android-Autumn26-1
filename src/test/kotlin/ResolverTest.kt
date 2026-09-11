import model.Player
import model.Position
import model.Team
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolverTest {
    private val alpha = Team("Alpha", "Berlin")
    private val beta = Team("Beta", "Munich")
    private val gamma = Team("Gamma", "Hamburg")

    @Test
    fun `counts players without agency`() {
        val resolver = Resolver(
            listOf(
                player("A", agency = null),
                player("B", agency = "Agency"),
            ),
        )

        assertEquals(1, resolver.getCountWithoutAgency())
    }

    @Test
    fun `best scorer defender is selected only among defenders`() {
        val resolver = Resolver(
            listOf(
                player("Forward", position = Position.FORWARD, goals = 30),
                player("Defender One", position = Position.DEFENDER, goals = 4),
                player("Defender Two", position = Position.DEFENDER, goals = 9),
            ),
        )

        assertEquals("Defender Two" to 9, resolver.getBestScorerDefender())
    }

    @Test
    fun `returns russian position name of most expensive german player`() {
        val resolver = Resolver(
            listOf(
                player("German Defender", position = Position.DEFENDER, country = "Germany", transferCost = 100),
                player("German Mid", position = Position.MIDFIELD, country = "Germany", transferCost = 200),
                player("Brazil Forward", position = Position.FORWARD, country = "Brazil", transferCost = 500),
            ),
        )

        assertEquals("полузащитник", resolver.getTheExpensiveGermanPlayerPosition())
    }

    @Test
    fun `returns team with highest average red cards`() {
        val resolver = Resolver(
            listOf(
                player("A1", team = alpha, redCards = 2),
                player("A2", team = alpha, redCards = 4),
                player("B1", team = beta, redCards = 5),
            ),
        )

        assertEquals(beta, resolver.getTheRudestTeam())
    }

    @Test
    fun `average transfer cost by position is sorted descending`() {
        val resolver = Resolver(
            listOf(
                player("Forward", position = Position.FORWARD, transferCost = 100),
                player("Defender", position = Position.DEFENDER, transferCost = 300),
                player("Mid One", position = Position.MIDFIELD, transferCost = 40),
                player("Mid Two", position = Position.MIDFIELD, transferCost = 60),
            ),
        )

        val result = resolver.getAverageTransferCostByPosition()

        assertEquals(listOf(Position.DEFENDER, Position.FORWARD, Position.MIDFIELD), result.keys.toList())
        assertEquals(300.0, result.getValue(Position.DEFENDER))
        assertEquals(100.0, result.getValue(Position.FORWARD))
        assertEquals(50.0, result.getValue(Position.MIDFIELD))
    }

    @Test
    fun `most valuable players use usefulness and name tie break`() {
        val resolver = Resolver(
            listOf(
                player("Charlie", goals = 2, assists = 2),
                player("Alice", goals = 4, assists = 1),
                player("Bob", goals = 1, assists = 3),
                player("David", goals = 1, assists = 1),
            ),
        )

        assertEquals(listOf("Bob", "Alice", "Charlie"), resolver.getMostValuablePlayers().map { it.name })
    }

    @Test
    fun `most popular agency by country uses alphabetical tie break`() {
        val resolver = Resolver(
            listOf(
                player("A1", country = "Germany", agency = "Beta"),
                player("A2", country = "Germany", agency = "Alpha"),
                player("A3", country = "Germany", agency = null),
                player("B1", country = "Brazil", agency = "Solo"),
                player("B2", country = "Brazil", agency = "Solo"),
                player("C1", country = "Chile", agency = null),
            ),
        )

        assertEquals(
            mapOf("Germany" to "Alpha", "Brazil" to "Solo"),
            resolver.getMostPopularAgencyByCountry(),
        )
    }

    @Test
    fun `goals share by median transfer cost is calculated correctly`() {
        val resolver = Resolver(
            listOf(
                player("Cheap", transferCost = 10, goals = 1),
                player("Middle", transferCost = 20, goals = 3),
                player("Expensive", transferCost = 30, goals = 6),
            ),
        )

        val result = resolver.getGoalsShareByMedianCost()

        assertEquals(0.4, result.first, 0.0001)
        assertEquals(0.6, result.second, 0.0001)
    }

    private fun player(
        name: String,
        team: Team = gamma,
        position: Position = Position.FORWARD,
        country: String = "Germany",
        agency: String? = "Agency",
        transferCost: Long = 100,
        participations: Int = 10,
        goals: Int = 0,
        assists: Int = 0,
        yellowCards: Int = 0,
        redCards: Int = 0,
    ): Player =
        Player(
            name = name,
            team = team,
            position = position,
            country = country,
            agency = agency,
            transferCost = transferCost,
            participations = participations,
            goals = goals,
            assists = assists,
            yellowCards = yellowCards,
            redCards = redCards,
        )
}
