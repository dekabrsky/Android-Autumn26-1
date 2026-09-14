import kotlin.test.Test
import kotlin.test.assertEquals

import model.Player
import model.Position
import model.Team
import resolver.Resolver

class ResolverTest {

    private val teamA = Team(
        name = "Team A",
        city = "Berlin"
    )

    private val teamB = Team(
        name = "Team B",
        city = "Munich"
    )

    private val players = listOf(
        Player(
            name = "Alice Forward",
            team = teamA,
            position = Position.FORWARD,
            nationality = "Germany",
            agency = null,
            transferCost = 10_000_000,
            participations = 10,
            goals = 20,
            assists = 5,
            yellowCards = 1,
            redCards = 0
        ),

        Player(
            name = "Bob Defender",
            team = teamA,
            position = Position.DEFENDER,
            nationality = "Germany",
            agency = "Agency A",
            transferCost = 20_000_000,
            participations = 12,
            goals = 15,
            assists = 3,
            yellowCards = 4,
            redCards = 1
        ),

        Player(
            name = "Charlie Midfielder",
            team = teamB,
            position = Position.MIDFIELD,
            nationality = "France",
            agency = "Agency A",
            transferCost = 30_000_000,
            participations = 15,
            goals = 5,
            assists = 20,
            yellowCards = 2,
            redCards = 0
        )
    )

    private val resolver = Resolver(players)

    @Test
    fun `getCountWithoutAgency counts players without agency`() {
        assertEquals(
            1,
            resolver.getCountWithoutAgency()
        )
    }

    @Test
    fun `getBestScorerDefender returns defender with most goals`() {
        assertEquals(
            "Bob Defender" to 15,
            resolver.getBestScorerDefender()
        )
    }

    @Test
    fun `getTheExpensiveGermanPlayerPosition returns position of most expensive German player`() {
        assertEquals(
            "защитник",
            resolver.getTheExpensiveGermanPlayerPosition()
        )
    }

    @Test
    fun `getTheRudestTeam returns team with highest average red cards`() {
        assertEquals(
            teamA,
            resolver.getTheRudestTeam()
        )
    }

    @Test
    fun `getAverageTransferCostByPosition returns average cost sorted descending`() {
        val result = resolver.getAverageTransferCostByPosition()

        assertEquals(
            listOf(Position.MIDFIELD, Position.DEFENDER, Position.FORWARD),
            result.keys.toList()
        )

        assertEquals(30_000_000.0, result[Position.MIDFIELD])
        assertEquals(20_000_000.0, result[Position.DEFENDER])
        assertEquals(10_000_000.0, result[Position.FORWARD])
    }

    @Test
    fun `getMostValuablePlayers returns top three by goals and assists formula`() {
        val result = resolver.getMostValuablePlayers()

        assertEquals(3, result.size)

        assertEquals("Charlie Midfielder", result[0].name)
        assertEquals("Alice Forward", result[1].name)
        assertEquals("Bob Defender", result[2].name)
    }

    @Test
    fun `getMostPopularAgencyByCountry returns most popular agency`() {
        val result = resolver.getMostPopularAgencyByCountry()

        assertEquals("Agency A", result["Germany"])
        assertEquals("Agency A", result["France"])
    }

    @Test
    fun `getGoalsShareByMedianCost returns shares for players not more expensive than median`() {
        val result = resolver.getGoalsShareByMedianCost()

        assertEquals(0.875, result.first, 0.0001)
        assertEquals(0.125, result.second, 0.0001)
    }
}