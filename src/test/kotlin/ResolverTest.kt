import model.Player
import model.Position
import model.Team
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ResolverTest {

    private val teamA = Team("Team A", "City A")
    private val teamB = Team("Team B", "City B")

    private fun player(
        name: String,
        team: Team = teamA,
        position: Position = Position.FORWARD,
        country: String = "Germany",
        agency: String? = "Agency A",
        cost: Long = 10_000_000,
        matches: Int = 20,
        goals: Int = 0,
        assists: Int = 0,
        yellow: Int = 0,
        red: Int = 0
    ) = Player(
        name = name,
        team = team,
        position = position,
        country = country,
        agency = agency,
        transferCost = cost,
        matches = matches,
        goals = goals,
        assists = assists,
        yellowCards = yellow,
        redCards = red
    )

    @Test
    fun `counts players without agency`() {
        val resolver = Resolver(
            listOf(
                player("A", agency = null),
                player("B", agency = "X"),
                player("C", agency = null)
            )
        )

        assertEquals(2, resolver.getCountWithoutAgency())
    }

    @Test
    fun `finds defender with most goals`() {
        val resolver = Resolver(
            listOf(
                player("A", position = Position.DEFENDER, goals = 3),
                player("B", position = Position.DEFENDER, goals = 8),
                player("C", position = Position.FORWARD, goals = 20)
            )
        )

        assertEquals(
            "B" to 8,
            resolver.getBestScorerDefender()
        )
    }

    @Test
    fun `finds position of most expensive German player`() {
        val resolver = Resolver(
            listOf(
                player(
                    "A",
                    position = Position.DEFENDER,
                    country = "Germany",
                    cost = 10
                ),
                player(
                    "B",
                    position = Position.FORWARD,
                    country = "Germany",
                    cost = 30
                ),
                player(
                    "C",
                    position = Position.GOALKEEPER,
                    country = "France",
                    cost = 100
                )
            )
        )

        assertEquals(
            "нападающий",
            resolver.getTheExpensiveGermanPlayerPosition()
        )
    }

    @Test
    fun `finds team with highest average red cards`() {
        val resolver = Resolver(
            listOf(
                player("A", team = teamA, red = 0),
                player("B", team = teamA, red = 2),
                player("C", team = teamB, red = 2)
            )
        )

        assertEquals(
            teamB,
            resolver.getTheRudestTeam()
        )
    }

    @Test
    fun `average costs are sorted descending`() {
        val resolver = Resolver(
            listOf(
                player("A", position = Position.FORWARD, cost = 100),
                player("B", position = Position.FORWARD, cost = 300),
                player("C", position = Position.DEFENDER, cost = 50)
            )
        )

        val result = resolver.getAverageTransferCostByPosition()

        assertEquals(
            listOf(Position.FORWARD, Position.DEFENDER),
            result.keys.toList()
        )
        assertEquals(200.0, result[Position.FORWARD])
        assertEquals(50.0, result[Position.DEFENDER])
    }

    @Test
    fun `top valuable players use assists with double weight and alphabetical tie break`() {
        val resolver = Resolver(
            listOf(
                player("Charlie", goals = 4, assists = 1),
                player("Alice", goals = 2, assists = 2),
                player("Bob", goals = 6, assists = 1),
                player("David", goals = 1, assists = 0)
            )
        )

        assertEquals(
            listOf("Bob", "Alice", "Charlie"),
            resolver.getMostValuablePlayers().map { it.name }
        )
    }

    @Test
    fun `most popular agency uses alphabetical tie break`() {
        val resolver = Resolver(
            listOf(
                player("A", country = "Germany", agency = "Zoo"),
                player("B", country = "Germany", agency = "Alpha"),
                player("C", country = "France", agency = "French Agency")
            )
        )

        val result = resolver.getMostPopularAgencyByCountry()

        assertEquals("Alpha", result["Germany"])
        assertEquals("French Agency", result["France"])
    }

    @Test
    fun `calculates goals share by median cost`() {
        val resolver = Resolver(
            listOf(
                player("A", cost = 10, goals = 2),
                player("B", cost = 20, goals = 3),
                player("C", cost = 30, goals = 5)
            )
        )

        val result = resolver.getGoalsShareByMedianCost()

        assertEquals(0.5, result.first, 0.000001)
        assertEquals(0.5, result.second, 0.000001)
    }
}