import model.Position
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolverTest {

    private val resolver = Resolver(TestPlayers.ALL)

    @Test
    fun `counts players without an agency`() {
        assertEquals(2, resolver.getCountWithoutAgency())
    }

    @Test
    fun `best scoring defender is picked alphabetically when goals are equal`() {
        assertEquals("Alpha" to 10, resolver.getBestScorerDefender())
    }

    @Test
    fun `position of the most expensive german player`() {
        assertEquals("защитник", resolver.getTheExpensiveGermanPlayerPosition())
    }

    @Test
    fun `rudest team is the one with the highest average of red cards`() {
        // Blues: (6 + 2 + 0) / 3 = 2.67, Reds: (4 + 0) / 2 = 2.0
        assertEquals(TestPlayers.BLUES, resolver.getTheRudestTeam())
    }

    @Test
    fun `average transfer cost by position is sorted descending`() {
        val actual = resolver.getAverageTransferCostByPosition()

        assertEquals(
            listOf(Position.MIDFIELD, Position.GOALKEEPER, Position.FORWARD, Position.DEFENDER),
            actual.keys.toList(),
        )
        assertEquals(500.0, actual.getValue(Position.MIDFIELD))
        assertEquals(400.0, actual.getValue(Position.GOALKEEPER))
        assertEquals(250.0, actual.getValue(Position.FORWARD))
        assertEquals(200.0, actual.getValue(Position.DEFENDER))
    }

    @Test
    fun `most valuable players are top three by goals plus double assists`() {
        val actual = resolver.getMostValuablePlayers()

        // Charlie 5 + 2*7 = 19, Echo 15, Alpha 10 + 2*1 = 12 (Bravo has the same 12, but later by name)
        assertEquals(listOf("Charlie", "Echo", "Alpha"), actual.map { it.name })
        assertEquals(listOf(19, 15, 12), actual.map { it.usefulness })
    }

    @Test
    fun `most popular agency per country ignores players without an agency`() {
        val actual = resolver.getMostPopularAgencyByCountry()

        // Brazil: Agency A и Agency B по одному, побеждает первое по алфавиту.
        // Spain выпадает: у единственного испанца нет агентства.
        assertEquals(mapOf("Brazil" to "Agency A", "Germany" to "Agency A"), actual)
    }

    @Test
    fun `goals are split by the median transfer cost`() {
        // Стоимости: 100, 250, 300, 400, 500 -> медиана 300.
        assertEquals(300.0, resolver.medianTransferCost())

        val (cheapShare, expensiveShare) = resolver.getGoalsShareByMedianCost()

        // Всего голов 40, у игроков не дороже медианы 10 + 5 + 10 = 25.
        assertEquals(0.625, cheapShare)
        assertEquals(0.375, expensiveShare)
        assertEquals(1.0, cheapShare + expensiveShare)
    }

    @Test
    fun `median of an even number of players is an average of two middle values`() {
        val evenResolver = Resolver(
            listOf(TestPlayers.ALPHA, TestPlayers.BRAVO, TestPlayers.CHARLIE, TestPlayers.DELTA)
        )

        // 100, 250, 300, 400 -> (250 + 300) / 2
        assertEquals(275.0, evenResolver.medianTransferCost())
    }

    @Test
    fun `shares are zero when nobody scored`() {
        val scoreless = Resolver(
            listOf(
                TestPlayers.player(name = "Nil", transferCost = 10),
                TestPlayers.player(name = "Zero", transferCost = 20),
            )
        )

        assertEquals(0.0 to 0.0, scoreless.getGoalsShareByMedianCost())
    }
}
