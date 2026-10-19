import model.Player
import model.Position
import model.Team
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolverTest {
    @Test
    fun `counts players without agency`() {
        assertEquals(2, resolver.getCountWithoutAgency())
    }

    @Test
    fun `finds best scorer defender`() {
        assertEquals("Bob" to 8, resolver.getBestScorerDefender())
    }

    @Test
    fun `returns position name of most expensive german player`() {
        assertEquals("полузащитник", resolver.getTheExpensiveGermanPlayerPosition())
    }

    @Test
    fun `finds team with highest average red cards`() {
        assertEquals(teamB, resolver.getTheRudestTeam())
    }

    @Test
    fun `team metrics use only players of this resolver`() {
        assertEquals(1.0, resolver.getAverageRedCards(teamA))
        assertEquals(300L, resolver.getTotalTransferCost(teamA))

        val subsetResolver = Resolver(listOf(players.first()))
        assertEquals(0.0, subsetResolver.getAverageRedCards(teamA))
        assertEquals(100L, subsetResolver.getTotalTransferCost(teamA))
        assertEquals(1.0, resolver.getAverageRedCards(teamA))
        assertEquals(300L, resolver.getTotalTransferCost(teamA))
    }

    @Test
    fun `returns average transfer cost by position sorted descending`() {
        val averageCostByPosition = resolver.getAverageTransferCostByPosition()

        assertEquals(
            mapOf(
                Position.MIDFIELD to 300.0,
                Position.GOALKEEPER to 250.0,
                Position.FORWARD to 200.0,
                Position.DEFENDER to 150.0,
            ),
            averageCostByPosition,
        )
        assertEquals(
            listOf(Position.MIDFIELD, Position.GOALKEEPER, Position.FORWARD, Position.DEFENDER),
            averageCostByPosition.keys.toList(),
        )
    }

    @Test
    fun `returns top three valuable players with alphabetical tiebreaker`() {
        assertEquals(
            listOf("Alice", "Carol", "Eve"),
            resolver.getMostValuablePlayers().map { it.name },
        )
    }

    @Test
    fun `returns most popular agency by country with alphabetical tiebreaker`() {
        assertEquals(
            mapOf(
                "Germany" to "Agency A",
                "Brazil" to "Agency B",
            ),
            resolver.getMostPopularAgencyByCountry(),
        )
    }

    @Test
    fun `returns goals share split by median cost`() {
        assertEquals(24.0 / 34.0 to 10.0 / 34.0, resolver.getGoalsShareByMedianCost())
    }

    private companion object {
        val teamA = Team("Team A", "City A")
        val teamB = Team("Team B", "City B")
        val teamC = Team("Team C", "City C")

        val players = listOf(
            player(
                name = "Alice",
                team = teamA,
                position = Position.FORWARD,
                nationality = "Germany",
                agency = "Agency A",
                transferCost = 100,
                goals = 10,
                assists = 5,
                redCards = 0,
            ),
            player(
                name = "Bob",
                team = teamA,
                position = Position.DEFENDER,
                nationality = "Brazil",
                agency = "Agency B",
                transferCost = 200,
                goals = 8,
                assists = 2,
                redCards = 2,
            ),
            player(
                name = "Carol",
                team = teamB,
                position = Position.DEFENDER,
                nationality = "Brazil",
                agency = "Agency C",
                transferCost = 100,
                goals = 6,
                assists = 4,
                redCards = 4,
            ),
            player(
                name = "Dave",
                team = teamB,
                position = Position.MIDFIELD,
                nationality = "Germany",
                agency = null,
                transferCost = 300,
                goals = 4,
                assists = 1,
                redCards = 0,
            ),
            player(
                name = "Eve",
                team = teamC,
                position = Position.GOALKEEPER,
                nationality = "Germany",
                agency = "Agency A",
                transferCost = 250,
                goals = 6,
                assists = 4,
                redCards = 1,
            ),
            player(
                name = "Frank",
                team = teamC,
                position = Position.FORWARD,
                nationality = "Brazil",
                agency = null,
                transferCost = 300,
                goals = 0,
                assists = 0,
                redCards = 1,
            ),
        )

        val resolver = Resolver(players)

        fun player(
            name: String,
            team: Team,
            position: Position,
            nationality: String,
            agency: String?,
            transferCost: Long,
            goals: Int,
            assists: Int,
            redCards: Int,
        ): Player = Player(
            name = name,
            team = team,
            position = position,
            nationality = nationality,
            agency = agency,
            transferCost = transferCost,
            participations = 10,
            goals = goals,
            assists = assists,
            yellowCards = 0,
            redCards = redCards,
        )
    }
}
