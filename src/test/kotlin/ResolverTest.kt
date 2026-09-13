import model.Player
import model.Position
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolverTest {
    private fun samplePlayers() = listOf(
        Player(
            name = "Anna Schmidt",
            teamName = "Nevada whales",
            city = "South Carolina",
            position = Position.DEFENDER,
            nationality = "Germany",
            agency = "Agency A",
            transferCost = 5_000_000,
            participations = 20,
            goals = 3,
            assists = 1,
            yellowCards = 2,
            redCards = 1,
        ),
        Player(
            name = "Boris Muller",
            teamName = "Nevada whales",
            city = "South Carolina",
            position = Position.DEFENDER,
            nationality = "Germany",
            agency = "Agency A",
            transferCost = 9_000_000,
            participations = 25,
            goals = 5,
            assists = 0,
            yellowCards = 1,
            redCards = 3,
        ),
        Player(
            name = "Carlos Lima",
            teamName = "Hawaii oxen",
            city = "Oklahoma",
            position = Position.FORWARD,
            nationality = "Brazil",
            agency = null,
            transferCost = 3_000_000,
            participations = 15,
            goals = 10,
            assists = 4,
            yellowCards = 0,
            redCards = 0,
        ),
    )

    private val resolver = Resolver(samplePlayers())

    @Test
    fun `1 counts players without agency`() {
        assertEquals(1, resolver.getCountWithoutAgency())
    }

    @Test
    fun `2 finds best scorer among defenders`() {
        assertEquals("Boris Muller" to 5, resolver.getBestScorerDefender())
    }

    @Test
    fun `3 finds position of the most expensive german player`() {
        assertEquals("защитник", resolver.getTheExpensiveGermanPlayerPosition())
    }

    @Test
    fun `4 finds the team with the highest average red cards`() {
        assertEquals("Nevada whales", resolver.getTheRudestTeam().name)
    }

    @Test
    fun `5 computes average transfer cost by position sorted descending`() {
        val result = resolver.getAverageTransferCostByPosition()
        assertEquals(listOf(Position.DEFENDER, Position.FORWARD), result.keys.toList())
        assertEquals(7_000_000.0, result[Position.DEFENDER])
        assertEquals(3_000_000.0, result[Position.FORWARD])
    }

    @Test
    fun `6 finds top-3 most valuable players ordered by impact then name`() {
        val names = resolver.getMostValuablePlayers().map { it.name }
        assertEquals(listOf("Carlos Lima", "Anna Schmidt", "Boris Muller"), names)
    }

    @Test
    fun `7 finds most popular agency by country`() {
        val result = resolver.getMostPopularAgencyByCountry()
        assertEquals("Agency A", result["Germany"])
        assertEquals(null, result["Brazil"])
    }

    @Test
    fun `8 splits goals share by median transfer cost`() {
        val (notAboveMedian, aboveMedian) = resolver.getGoalsShareByMedianCost()
        assertEquals(13.0 / 18.0, notAboveMedian)
        assertEquals(5.0 / 18.0, aboveMedian)
    }
}
