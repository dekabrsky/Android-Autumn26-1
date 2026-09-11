package resolver

import model.Player
import model.Position
import model.Team
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResolverTest {

    private val team = Team(name = "name", city = "city")

    @Test
    fun `get count of players without agency`(){
        val resolver = Resolver(listOf(
            Player("name1", team, Position.FORWARD, "nationality", null, 11, 11, 11, 11, 11, 11),
            Player("name2", team, Position.FORWARD, "nationality", "agency", 11, 11, 11, 11, 11, 11)
        ))

        val result = resolver.getCountWithoutAgency()
        assertEquals(1, result)
    }

    @Test
    fun `get defender with best score`(){
        val resolver = Resolver(listOf(
            Player("bestScorerDefender", team, Position.DEFENDER, "Germany", null, 1_000, 11, 100, 11, 11, 11),
            Player("notBestScorerDefender", team, Position.DEFENDER, "Germany", null, 100, 11, 99, 11, 11, 11),
            Player("notDefender", team, Position.FORWARD, "Germany", null, 100, 11, 1000, 11, 11, 11)
        ))

        val resultBestScorerDefenderData = resolver.getBestScorerDefender()
        assertEquals("bestScorerDefender", resultBestScorerDefenderData.first)
        assertEquals(100, resultBestScorerDefenderData.second)
    }

    @Test
    fun `Defenders not found in getBestScorerDefender`(){
        val resolver = Resolver(listOf(
            Player("bestScorerDefender", team, Position.FORWARD, "Germany", null, 1_000, 11, 100, 11, 11, 11),
            Player("notBestScorerDefender", team, Position.FORWARD, "Germany", null, 100, 11, 99, 11, 11, 11)
        ))

        val exception = assertThrows(RuntimeException::class.java) {
            resolver.getBestScorerDefender()
        }
        assertEquals("Defenders not found", exception.message)
    }

    @Test
    fun `get russian name of most expensive german player position`(){
        val resolver = Resolver(listOf(
            Player("name1", team, Position.FORWARD, "Germany", null, 1_000, 11, 11, 11, 11, 11),
            Player("name1", team, Position.DEFENDER, "Germany", null, 100, 11, 11, 11, 11, 11),
            Player("name1", team, Position.GOALKEEPER, "Russia", null, 10_000, 11, 11, 11, 11, 11)
        ))
        assertEquals("нападающий", resolver.getTheExpensiveGermanPlayerPosition())
    }

    @Test
    fun `get rudest team`(){
        val rudestTeam = Team(name = "rudestTeam", city = "rudestTeamCity")
        val notRudestTeam = Team(name = "notRudestTeam", city = "notRudestTeamCity")
        val resolver = Resolver(listOf(
            Player("name1", rudestTeam, Position.FORWARD, "Germany", null, 1_000, 11, 11, 11, 11, 100),
            Player("name1", rudestTeam, Position.DEFENDER, "Germany", null, 100, 11, 11, 11, 11, 1),
            Player("name1", notRudestTeam, Position.GOALKEEPER, "Russia", null, 10_000, 11, 11, 11, 11, 40),
            Player("name1", notRudestTeam, Position.GOALKEEPER, "Russia", null, 10_000, 11, 11, 11, 11, 60)
        ))

        val resultRudestTeam = resolver.getTheRudestTeam()
        assertEquals(rudestTeam.name, resultRudestTeam.name)
        assertEquals(rudestTeam.city, resultRudestTeam.city)
    }

    @Test
    fun `Teams not found in getTheRudestTeam`(){
        val resolver = Resolver(emptyList())
        val exception = assertThrows(RuntimeException::class.java) {
            resolver.getTheRudestTeam()
        }

        assertEquals("Teams not found", exception.message)
    }

    @Test
    fun `get average transfer cost by position`() {
        val players = listOf(
            Player("Player 1", team, Position.FORWARD, "Russia", null, 100, 10, 5, 2, 1, 0),
            Player("Player 2", team, Position.FORWARD, "Russia", null, 200, 10, 5, 2, 1, 0),
            Player("Player 3", team, Position.DEFENDER, "Russia", null, 300, 10, 5, 2, 1, 0),
            Player("Player 4", team, Position.MIDFIELD, "Russia", null, 150, 10, 5, 2, 1, 0),
            Player("Player 5", team, Position.MIDFIELD, "Russia", null, 250, 10, 5, 2, 1, 0)
        )

        val resolver = Resolver(players)
        val result = resolver.getAverageTransferCostByPosition()
        assertEquals(
            mapOf(
                Position.DEFENDER to 300.0,
                Position.MIDFIELD to 200.0,
                Position.FORWARD to 150.0
            ),
            result
        )
        assertEquals(
            listOf(Position.DEFENDER, Position.MIDFIELD, Position.FORWARD),
            result.keys.toList()
        )
    }

    @Test
    fun `return empty map when players list is empty`() {
        val resolver = Resolver(emptyList())
        val result = resolver.getAverageTransferCostByPosition()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `get most valuable players`() {
        val players = listOf(
            Player("player3", team, Position.DEFENDER, "Russia", null, 100, 10, 12, 3, 0, 0),
            Player("player4", team, Position.MIDFIELD, "Russia", null, 100, 10, 5, 4, 0, 0),
            Player("player1", team, Position.FORWARD, "Russia", null, 100, 10, 10, 5, 0, 0),
            Player("player2", team, Position.FORWARD, "Russia", null, 100, 10, 8, 6, 0, 0)
            )

        val resolver = Resolver(players)
        val result = resolver.getMostValuablePlayers()
        assertEquals(listOf(players[2], players[3], players[0]), result)
    }

    @Test
    fun `get most popular agency by country`() {
        val players = listOf(
            Player("player1", team, Position.FORWARD, "Russia", "Agency B", 100, 10, 5, 2, 0, 0),
            Player("player2", team, Position.DEFENDER, "Russia", "Agency A", 100, 10, 3, 1, 0, 0),
            Player("player3", team, Position.MIDFIELD, "Russia", "Agency A", 100, 10, 4, 2, 0, 0),
            Player("player4", team, Position.FORWARD, "Germany", "Agency C", 100, 10, 5, 2, 0, 0),
            Player("player5", team, Position.DEFENDER, "Germany", "Agency D", 100, 10, 3, 1, 0, 0),
            Player("player6", team, Position.MIDFIELD, "Germany", "Agency C", 100, 10, 4, 2, 0, 0),
            Player("player7", team, Position.DEFENDER, "Germany", "Agency D", 100, 10, 3, 1, 0, 0),

        )

        val resolver = Resolver(players)
        val result = resolver.getMostPopularAgencyByCountry()
        assertEquals(mapOf("Russia" to "Agency A", "Germany" to "Agency C",), result)
    }

    @Test
    fun `return empty map when agencies are not found`() {
        val resolver = Resolver(listOf(
            Player("player1", team, Position.FORWARD, "Russia", null, 100, 10, 5, 2, 0, 0),
            Player("player2", team, Position.DEFENDER, "Germany", null, 200, 10, 3, 1, 0, 0)
        ))

        val result = resolver.getMostPopularAgencyByCountry()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `get goals share by median cost with odd number of players`() {
        val players = listOf(
            Player("player1", team, Position.FORWARD, "Russia", null, 100, 10, 5, 2, 0, 0),
            Player("player2", team, Position.FORWARD, "Russia", null, 200, 10, 3, 2, 0, 0),
            Player("player3", team, Position.FORWARD, "Russia", null, 300, 10, 2, 1, 0, 0),
            Player("player4", team, Position.FORWARD, "Russia", null, 400, 10, 4, 1, 0, 0),
            Player("player5", team, Position.FORWARD, "Russia", null, 500, 10, 1, 1, 0, 0)
        )

        val resolver = Resolver(players)
        val result = resolver.getGoalsShareByMedianCost()
        assertEquals(10.0 / 15.0 to 5.0 / 15.0, result)
    }

    @Test
    fun `get goals share by median cost with even number of players`() {
        val players = listOf(
            Player("player1", team, Position.FORWARD, "Russia", null, 100, 10, 6, 2, 0, 0),
            Player("player2", team, Position.FORWARD, "Russia", null, 200, 10, 2, 2, 0, 0),
            Player("player3", team, Position.FORWARD, "Russia", null, 300, 10, 4, 1, 0, 0),
            Player("player4", team, Position.FORWARD, "Russia", null, 500, 10, 3, 1, 0, 0)
        )

        val resolver = Resolver(players)
        val result = resolver.getGoalsShareByMedianCost()
        assertEquals(8.0 / 15.0, result.first)
        assertEquals(7.0 / 15.0, result.second)
    }

    @Test
    fun `get goals share by median cost when total goals are zero`() {
        val players = listOf(
            Player("player1", team, Position.FORWARD, "Russia", null, 100, 10, 0, 2, 0, 0),
            Player("player2", team, Position.DEFENDER, "Russia", null, 200, 10, 0, 1, 0, 0),
            Player("player3", team, Position.MIDFIELD, "Russia", null, 300, 10, 0, 3, 0, 0)
        )

        val resolver = Resolver(players)
        val result = resolver.getGoalsShareByMedianCost()
        assertEquals(0.0 to 0.0, result)
    }
}