import charts.Charts
import model.Player
import model.Position
import model.Team
import kotlin.test.Test
import kotlin.test.assertEquals

class ChartsTest {

    private fun player(name: String, team: String, position: Position, cost: Long, goals: Int) =
        Player(name, Team(team, "City"), position, "Brazil", null, cost, 10, goals, 0, 0, 0)

    @Test
    fun `top teams by cost are sorted descending and limited`() {
        val players = listOf(
            player("A", "T1", Position.FORWARD, 100, 0),
            player("B", "T1", Position.DEFENDER, 100, 0),
            player("C", "T2", Position.FORWARD, 500, 0),
            player("D", "T3", Position.FORWARD, 50, 0),
        )
        assertEquals(listOf("T2" to 500L, "T1" to 200L), Charts.topTeamsByCost(players, limit = 2))
    }

    @Test
    fun `moving average uses windowed forwards sorted by cost`() {
        val players = listOf(
            player("A", "T", Position.FORWARD, 300, 3),
            player("B", "T", Position.FORWARD, 100, 1),
            player("C", "T", Position.DEFENDER, 50, 99),
            player("D", "T", Position.FORWARD, 200, 2),
            player("E", "T", Position.FORWARD, 400, 8),
        )
        val forwards = Charts.forwardsByCost(players)
        assertEquals(listOf("B", "D", "A", "E"), forwards.map { it.name })
        assertEquals(
            listOf(200.0 to 2.0, 300.0 to 13.0 / 3),
            Charts.movingAverage(forwards, window = 3),
        )
    }
}
