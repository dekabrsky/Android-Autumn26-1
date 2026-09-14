import chart.ChartData
import model.Team
import kotlin.test.Test
import kotlin.test.assertEquals

class ChartDataTest {

    @Test
    fun `country shares sum to one and are sorted by count`() {
        val players = listOf(
            player("A", nationality = "Ghana"),
            player("B", nationality = "Qatar"),
            player("C", nationality = "Qatar"),
            player("D", nationality = "Qatar"),
        )
        val shares = ChartData.countryShares(players)

        assertEquals(listOf("Qatar", "Ghana"), shares.map { it.country })
        assertEquals(listOf(3, 1), shares.map { it.playersCount })
        assertEquals(listOf(0.75, 0.25), shares.map { it.share })
    }

    @Test
    fun `top teams have cumulative share of total cost`() {
        val a = Team("A", "a")
        val b = Team("B", "b")
        val c = Team("C", "c")
        val players = listOf(
            player("A1", team = a, transferCost = 30),
            player("A2", team = a, transferCost = 20),
            player("B1", team = b, transferCost = 10),
            player("C1", team = c, transferCost = 40),
        )
        val top = ChartData.topTeamsByCost(players, limit = 2)

        assertEquals(listOf(a, c), top.map { it.team })
        assertEquals(listOf(50L, 40L), top.map { it.totalCost })
        assertEquals(listOf(0.5, 0.9), top.map { it.cumulativeShare })
    }
}
