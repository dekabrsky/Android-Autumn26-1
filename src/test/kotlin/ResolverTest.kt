import model.Player
import model.Position
import model.Team
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolverTest {

    private fun player(
        name: String,
        teamName: String = "Team A",
        city: String = "City",
        position: Position = Position.FORWARD,
        country: String = "Russia",
        agency: String? = "Agency",
        cost: Long = 100,
        goals: Int = 0,
        assists: Int = 0,
        redCards: Int = 0
    ): Player {
        return Player(
            name = name,
            team = Team(teamName, city),
            position = position,
            country = country,
            agency = agency,
            transferCost = cost,
            participations = 10,
            goals = goals,
            assists = assists,
            yellowCards = 0,
            redCards = redCards
        )
    }

    @Test
    fun `query 1 counts players without agency`() {
        val players = listOf(
            player("A", agency = null),
            player("B", agency = "Agency"),
            player("C", agency = null)
        )

        val resolver = Resolver(players)

        assertEquals(2, resolver.getCountWithoutAgency())
    }

    @Test
    fun `query 2 finds defender with most goals`() {
        val players = listOf(
            player("Alex", position = Position.DEFENDER, goals = 3),
            player("Bob", position = Position.DEFENDER, goals = 8),
            player("Carl", position = Position.FORWARD, goals = 20)
        )

        val resolver = Resolver(players)

        assertEquals(
            Pair("Bob", 8),
            resolver.getBestScorerDefender()
        )
    }

    @Test
    fun `query 3 finds position of most expensive German`() {
        val players = listOf(
            player(
                "A",
                position = Position.DEFENDER,
                country = "Germany",
                cost = 100
            ),
            player(
                "B",
                position = Position.GOALKEEPER,
                country = "Germany",
                cost = 500
            ),
            player(
                "C",
                position = Position.FORWARD,
                country = "France",
                cost = 1000
            )
        )

        val resolver = Resolver(players)

        assertEquals(
            "вратарь",
            resolver.getTheExpensiveGermanPlayerPosition()
        )
    }

    @Test
    fun `query 4 finds team with highest average red cards`() {
        val players = listOf(
            player("A", teamName = "Good", redCards = 0),
            player("B", teamName = "Good", redCards = 2),
            player("C", teamName = "Rude", redCards = 3)
        )

        val resolver = Resolver(players)

        assertEquals(
            Team("Rude", "City"),
            resolver.getTheRudestTeam()
        )
    }

    @Test
    fun `query 5 calculates average cost and sorts descending`() {
        val players = listOf(
            player("A", position = Position.FORWARD, cost = 100),
            player("B", position = Position.FORWARD, cost = 300),
            player("C", position = Position.DEFENDER, cost = 50)
        )

        val resolver = Resolver(players)

        val result = resolver.getAverageTransferCostByPosition()

        assertEquals(
            listOf(Position.FORWARD, Position.DEFENDER),
            result.keys.toList()
        )

        assertEquals(200.0, result[Position.FORWARD])
        assertEquals(50.0, result[Position.DEFENDER])
    }

    @Test
    fun `query 6 returns top three by usefulness and alphabet`() {
        val players = listOf(
            player("Charlie", goals = 5, assists = 0),
            player("Bob", goals = 4, assists = 1),
            player("Alice", goals = 4, assists = 1),
            player("David", goals = 1, assists = 0)
        )

        val resolver = Resolver(players)

        val result = resolver
            .getMostValuablePlayers()
            .map { it.name }

        assertEquals(
            listOf("Alice", "Bob", "Charlie"),
            result
        )
    }

    @Test
    fun `query 7 finds most popular agency and resolves tie alphabetically`() {
        val players = listOf(
            player("A", country = "Russia", agency = "Zoo"),
            player("B", country = "Russia", agency = "Alpha"),
            player("C", country = "Germany", agency = "Beta")
        )

        val resolver = Resolver(players)

        val result = resolver.getMostPopularAgencyByCountry()

        assertEquals("Alpha", result["Russia"])
        assertEquals("Beta", result["Germany"])
    }

    @Test
    fun `query 8 calculates goal shares around median`() {
        val players = listOf(
            player("A", cost = 10, goals = 1),
            player("B", cost = 20, goals = 2),
            player("C", cost = 30, goals = 7)
        )

        val resolver = Resolver(players)

        val result = resolver.getGoalsShareByMedianCost()

        assertEquals(0.3, result.first, 0.000001)
        assertEquals(0.7, result.second, 0.000001)
    }
}