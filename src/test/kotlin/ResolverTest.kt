import model.Player
import model.Position
import model.Team
import resolver.IResolver
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolverTest {

    @Test
    fun `query 1 counts players without agency`() {
        val resolver = resolverOf(
            player(name = "Without agency", agency = null),
            player(name = "With agency", agency = "Agency"),
            player(name = "Also without agency", agency = null),
        )

        assertEquals(2, resolver.getCountWithoutAgency())
    }

    @Test
    fun `query 2 finds defender with most goals`() {
        val resolver = resolverOf(
            player(name = "First defender", position = Position.DEFENDER, goals = 4),
            player(name = "Best defender", position = Position.DEFENDER, goals = 7),
            player(name = "Forward", position = Position.FORWARD, goals = 20),
        )

        assertEquals("Best defender" to 7, resolver.getBestScorerDefender())
    }

    @Test
    fun `query 3 returns Russian position of most expensive German player`() {
        val resolver = resolverOf(
            player(position = Position.DEFENDER, nationality = "Germany", transferCost = 10),
            player(position = Position.GOALKEEPER, nationality = "Germany", transferCost = 30),
            player(position = Position.FORWARD, nationality = "Brazil", transferCost = 100),
        )

        assertEquals("вратарь", resolver.getTheExpensiveGermanPlayerPosition())
    }

    @Test
    fun `query 4 compares average red cards rather than total`() {
        val teamA = Team("Team A", "City A")
        val teamB = Team("Team B", "City B")
        val resolver = resolverOf(
            player(name = "A1", team = teamA, redCards = 2),
            player(name = "A2", team = teamA, redCards = 2),
            player(name = "B1", team = teamB, redCards = 3),
        )

        assertEquals(teamB, resolver.getTheRudestTeam())
    }

    @Test
    fun `query 5 returns position averages in descending order`() {
        val resolver = resolverOf(
            player(position = Position.DEFENDER, transferCost = 10),
            player(position = Position.DEFENDER, transferCost = 30),
            player(position = Position.FORWARD, transferCost = 40),
            player(position = Position.GOALKEEPER, transferCost = 5),
        )

        val averages = resolver.getAverageTransferCostByPosition()
        assertEquals(
            listOf(Position.FORWARD, Position.DEFENDER, Position.GOALKEEPER),
            averages.keys.toList(),
        )
        assertEquals(40.0, averages.getValue(Position.FORWARD))
        assertEquals(20.0, averages.getValue(Position.DEFENDER))
        assertEquals(5.0, averages.getValue(Position.GOALKEEPER))
    }

    @Test
    fun `query 6 sorts by usefulness and resolves ties alphabetically`() {
        val resolver = resolverOf(
            player(name = "Zed", goals = 5, assists = 0),
            player(name = "Alice", goals = 1, assists = 2),
            player(name = "Bob", goals = 4, assists = 3),
            player(name = "Carol", goals = 2, assists = 3),
        )

        assertEquals(
            listOf("Bob", "Carol", "Alice"),
            resolver.getMostValuablePlayers().map { it.name },
        )
    }

    @Test
    fun `query 7 finds popular agency and resolves ties alphabetically`() {
        val resolver = resolverOf(
            player(nationality = "Germany", agency = "Z Agency"),
            player(nationality = "Germany", agency = "A Agency"),
            player(nationality = "Germany", agency = null),
            player(nationality = "Brazil", agency = "C Agency"),
            player(nationality = "Brazil", agency = "C Agency"),
            player(nationality = "Brazil", agency = "D Agency"),
        )

        assertEquals(
            mapOf("Brazil" to "C Agency", "Germany" to "A Agency"),
            resolver.getMostPopularAgencyByCountry(),
        )
    }

    @Test
    fun `query 8 calculates goal shares using median transfer cost`() {
        val resolver = resolverOf(
            player(transferCost = 10, goals = 1),
            player(transferCost = 20, goals = 3),
            player(transferCost = 30, goals = 2),
            player(transferCost = 40, goals = 4),
        )

        val (notMoreExpensive, moreExpensive) = resolver.getGoalsShareByMedianCost()
        assertEquals(0.4, notMoreExpensive, absoluteTolerance = 1e-12)
        assertEquals(0.6, moreExpensive, absoluteTolerance = 1e-12)
    }

    private fun resolverOf(vararg players: Player): IResolver = Resolver(players.toList())

    private fun player(
        name: String = "Player",
        team: Team = Team("Team", "City"),
        position: Position = Position.FORWARD,
        nationality: String = "Germany",
        agency: String? = "Agency",
        transferCost: Int = 1,
        goals: Int = 0,
        assists: Int = 0,
        redCards: Int = 0,
    ): Player = Player(
        name = name,
        team = team,
        position = position,
        nationality = nationality,
        agency = agency,
        transferCost = transferCost,
        participations = 1,
        goals = goals,
        assists = assists,
        yellowCards = 0,
        redCards = redCards,
    )
}
