

import kotlin.test.Test
import kotlin.test.assertEquals

import model.Player
import model.Position
import model.Team
import parser.parsePlayer
import parser.readCSV
import resolver.Resolver
import java.io.ByteArrayInputStream

class ResolverTest {

    private val teamA = Team("Team A", "City A")
    private val teamB = Team("Team B", "City B")

    private val players = listOf(
        Player(
            name = "Ivan",
            team = teamA,
            position = Position.DEFENDER,
            nationality = "Russia",
            agency = null,
            transferCost = 10,
            participations = 20,
            goals = 5,
            assists = 2,
            yellowCards = 1,
            redCards = 0
        ),
        Player(
            name = "Boris",
            team = teamA,
            position = Position.FORWARD,
            nationality = "Germany",
            agency = "Agency B",
            transferCost = 30,
            participations = 25,
            goals = 10,
            assists = 5,
            yellowCards = 2,
            redCards = 1
        ),
        Player(
            name = "Alex",
            team = teamB,
            position = Position.MIDFIELD,
            nationality = "Russia",
            agency = "Agency A",
            transferCost = 20,
            participations = 15,
            goals = 4,
            assists = 3,
            yellowCards = 0,
            redCards = 2
        )
    )

    private val resolver = Resolver(players)


    @Test
    fun `should count players without agency`() {
        assertEquals(
            1,
            resolver.getCountWithoutAgency()
        )
    }

    @Test
    fun `should find best scoring defender`() {
        assertEquals(
            "Ivan" to 5,
            resolver.getBestScorerDefender()
        )
    }

    @Test
    fun `should find position of most expensive German player`() {
        assertEquals(
            "нападающий",
            resolver.getTheExpensiveGermanPlayerPosition()
        )
    }

    @Test
    fun `should find rudest team by average red cards`() {
        assertEquals(
            teamB,
            resolver.getTheRudestTeam()
        )
    }

    @Test
    fun `should calculate average transfer cost by position`() {
        val result = resolver.getAverageTransferCostByPosition()

        println(result)

        assertEquals(30.0, result[Position.FORWARD])
        assertEquals(20.0, result[Position.MIDFIELD])
        assertEquals(10.0, result[Position.DEFENDER])
    }

    @Test
    fun `should return top 3 most valuable players`() {
        val result = resolver.getMostValuablePlayers()

        assertEquals(
            listOf("Boris", "Alex", "Ivan"),
            result.map { it.name }
        )
    }

    @Test
    fun `should find most popular agency by country`() {
        val result = resolver.getMostPopularAgencyByCountry()

        assertEquals(
            "Agency A",
            result["Russia"]
        )

        assertEquals(
            "Agency B",
            result["Germany"]
        )
    }

    @Test
    fun `should calculate goals share by median transfer cost`() {
        val result = resolver.getGoalsShareByMedianCost()

        assertEquals(
            9.0 / 19.0,
            result.first,
            0.000001
        )

        assertEquals(
            10.0 / 19.0,
            result.second,
            0.000001
        )
    }
}