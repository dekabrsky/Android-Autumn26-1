import model.Player
import model.Position
import model.Team
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolverTest {

    private fun buildPlayer(
        name: String,
        team: Team,
        position: Position,
        country: String,
        agency: String?,
        transferCost: Long,
        goals: Int,
        assists: Int,
        yellowCards: Int,
        redCards: Int
    ): Player {
        val player = Player(
            name = name,
            team = team,
            position = position,
            country = country,
            agency = agency,
            transferCost = transferCost,
            participations = 20,
            goals = goals,
            assists = assists,
            yellowCards = yellowCards,
            redCards = redCards
        )
        team.registerPlayer(player)
        return player
    }

    private val teamA = Team("Wolves", "X City")
    private val teamB = Team("Bears", "Y City")

    private val alice = buildPlayer(
        "Alice", teamA, Position.FORWARD, "Germany", "AgencyX",
        transferCost = 50, goals = 5, assists = 2, yellowCards = 1, redCards = 0
    )
    private val bob = buildPlayer(
        "Bob", teamA, Position.DEFENDER, "Brazil", null,
        transferCost = 30, goals = 3, assists = 1, yellowCards = 2, redCards = 1
    )
    private val carol = buildPlayer(
        "Carol", teamB, Position.DEFENDER, "Brazil", "AgencyY",
        transferCost = 70, goals = 7, assists = 0, yellowCards = 0, redCards = 3
    )
    private val dave = buildPlayer(
        "Dave", teamB, Position.GOALKEEPER, "Germany", "AgencyX",
        transferCost = 90, goals = 0, assists = 0, yellowCards = 1, redCards = 0
    )

    private val resolver = Resolver(listOf(alice, bob, carol, dave))

    @Test
    fun `count without agency`() {
        assertEquals(1, resolver.getCountWithoutAgency())
    }

    @Test
    fun `best scorer defender`() {
        assertEquals("Carol" to 7, resolver.getBestScorerDefender())
    }

    @Test
    fun `position of the most expensive german player`() {
        assertEquals("вратарь", resolver.getTheExpensiveGermanPlayerPosition())
    }

    @Test
    fun `rudest team by average red cards`() {
        assertEquals(teamB, resolver.getTheRudestTeam())
    }

    @Test
    fun `average transfer cost by position sorted descending`() {
        val result = resolver.getAverageTransferCostByPosition()
        val orderedPositions = result.keys.toList()

        assertEquals(Position.GOALKEEPER, orderedPositions.first())
        assertEquals(90.0, result[Position.GOALKEEPER])
        assertEquals(50.0, result[Position.FORWARD])
        assertEquals(50.0, result[Position.DEFENDER])
    }

    @Test
    fun `top 3 most valuable players`() {
        val top = resolver.getMostValuablePlayers()
        assertEquals(listOf("Alice", "Carol", "Bob"), top.map { it.name })
    }

    @Test
    fun `most popular agency by country`() {
        val result = resolver.getMostPopularAgencyByCountry()
        assertEquals("AgencyX", result["Germany"])
        assertEquals("AgencyY", result["Brazil"])
    }

    @Test
    fun `goals share by median cost`() {
        val (shareNotAbove, shareAbove) = resolver.getGoalsShareByMedianCost()

        assertEquals(8.0 / 15.0, shareNotAbove, 1e-9)
        assertEquals(7.0 / 15.0, shareAbove, 1e-9)
    }

    @Test
    fun `bonus filter-based queries`() {
        assertEquals(1, resolver.countCheapDefenders(40))
        assertEquals(2, resolver.countNonForwardsCheaperThan(80))
    }
}
