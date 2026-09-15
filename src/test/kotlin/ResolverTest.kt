import model.Player
import model.Position
import model.Team
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResolverTest {
    private fun player(
        name: String = "Player",
        teamName: String = "Team",
        city: String = "City",
        position: Position = Position.FORWARD,
        country: String = "Russia",
        agency: String? = null,
        transferCost: Int = 100,
        participations: Int = 10,
        goals: Int = 0,
        assists: Int = 0,
        yellowCards: Int = 0,
        redCards: Int = 0
    ) = Player(
        name = name,
        team = Team(teamName, city),
        position = position,
        country = country,
        agency = agency,
        transferCost = transferCost,
        participations = participations,
        goals = goals,
        assists = assists,
        yellowCards = yellowCards,
        redCards = redCards
    )


    @Test
    fun `counts players without agency`() {
        val players = listOf(
            player(name = "A", agency = null),
            player(name = "B", agency = "X"),
            player(name = "C", agency = null)
        )
        assertEquals(2, Resolver(players).getCountWithoutAgency())
    }

    @Test
    fun `zero when everyone has an agency`() {
        val players = listOf(
            player(name = "A", agency = "X"),
            player(name = "B", agency = "Y")
        )
        assertEquals(0, Resolver(players).getCountWithoutAgency())
    }

    @Test
    fun `finds best scorer defender ignoring forwards`() {
        val players = listOf(
            player(name = "Def1", position = Position.DEFENDER, goals = 5),
            player(name = "Def2", position = Position.DEFENDER, goals = 12),
            player(name = "Fwd",  position = Position.FORWARD,  goals = 30)
        )
        assertEquals("Def2" to 12, Resolver(players).getBestScorerDefender())
    }

    @Test
    fun `empty pair when there are no defenders`() {
        val players = listOf(
            player(name = "Fwd", position = Position.FORWARD, goals = 5),
            player(name = "Mid", position = Position.MIDFIELD, goals = 3)
        )
        assertEquals("" to 0, Resolver(players).getBestScorerDefender())
    }

    @Test
    fun `finds position of the most expensive german player`() {
        val players = listOf(
            player(name = "A", country = "Germany", position = Position.FORWARD,
                transferCost = 100),
            player(name = "B", country = "Germany", position = Position.DEFENDER,
                transferCost = 500),
            player(name = "C", country = "Brazil", position = Position.FORWARD,
                transferCost = 999)     // не немец — игнорируется
        )
        assertEquals("защитник",
            Resolver(players).getTheExpensiveGermanPlayerPosition())
    }

    @Test
    fun `empty string when no german players`() {
        val players = listOf(
            player(name = "A", country = "Brazil", position = Position.FORWARD)
        )
        assertEquals("", Resolver(players).getTheExpensiveGermanPlayerPosition())
    }

    @Test
    fun `finds team with highest average red cards`() {
        val players = listOf(
            player(teamName = "Rough", city = "CityA", redCards = 5),
            player(teamName = "Rough", city = "CityA", redCards = 1),
            player(teamName = "Calm",  city = "CityB", redCards = 1),
            player(teamName = "Calm",  city = "CityB", redCards = 1),
            player(teamName = "Calm",  city = "CityB", redCards = 1)
        )
        val rudest = Resolver(players).getTheRudestTeam()
        assertEquals("Rough", rudest.name)
        assertEquals("CityA", rudest.city)
    }

    @Test
    fun `a single team is trivially the rudest`() {
        val players = listOf(
            player(teamName = "OnlyTeam", city = "OnlyCity", redCards = 3),
            player(teamName = "OnlyTeam", city = "OnlyCity", redCards = 7)
        )
        val rudest = Resolver(players).getTheRudestTeam()
        assertEquals("OnlyTeam", rudest.name)
        assertEquals("OnlyCity", rudest.city)
    }

    @Test
    fun `averages transfer cost by position sorted descending`() {
        val players = listOf(
            player(name = "F1", position = Position.FORWARD,  transferCost = 100),
            player(name = "F2", position = Position.FORWARD,  transferCost = 200),
            player(name = "D1", position = Position.DEFENDER, transferCost = 300)
        )
        val result = Resolver(players).getAverageTransferCostByPosition()
        val entries = result.entries.toList()

        assertEquals(2, entries.size)
        assertEquals(Position.DEFENDER, entries[0].key)
        assertEquals(300.0, entries[0].value)
        assertEquals(Position.FORWARD, entries[1].key)
        assertEquals(150.0, entries[1].value)
    }

    @Test
    fun `single position yields single-entry map`() {
        val players = listOf(
            player(name = "F1", position = Position.FORWARD, transferCost = 100),
            player(name = "F2", position = Position.FORWARD, transferCost = 300)
        )
        val result = Resolver(players).getAverageTransferCostByPosition()
        assertEquals(1, result.size)
        assertEquals(200.0, result[Position.FORWARD])
    }

    @Test
    fun `top-3 valuable players sorted by goals plus 2 assists then by name`() {
        val players = listOf(
            player(name = "Zed",   goals = 10, assists = 0), // 10
            player(name = "Alpha", goals = 10, assists = 0), // 10
            player(name = "Beta",  goals = 5,  assists = 5), // 15
            player(name = "Gamma", goals = 0,  assists = 1)  // 2
        )
        val top = Resolver(players).getMostValuablePlayers()
        assertEquals(3, top.size)
        assertEquals("Beta",  top[0].name) // 15
        assertEquals("Alpha", top[1].name) // 10, Alpha < Zed по алфавиту
        assertEquals("Zed",   top[2].name) // 10
    }

    @Test
    fun `top-3 returns fewer when list has fewer players`() {
        val players = listOf(
            player(name = "A", goals = 1),
            player(name = "B", goals = 2)
        )
        val top = Resolver(players).getMostValuablePlayers()
        assertEquals(2, top.size)
        assertEquals("B", top[0].name)
        assertEquals("A", top[1].name)
    }

    @Test
    fun `most popular agency per country picks alphabetical on ties`() {
        val players = listOf(
            player(name = "A", country = "Colombia", agency = "Zeta"),
            player(name = "B", country = "Colombia", agency = "Alpha"),
            player(name = "C", country = "Colombia", agency = "Alpha"),
            player(name = "D", country = "Russia",   agency = "Beta"),
            player(name = "E", country = "Russia",   agency = "Alpha"),
            player(name = "F", country = "Russia",   agency = null)
        )
        val result = Resolver(players).getMostPopularAgencyByCountry()

        assertEquals("Alpha", result["Colombia"])
        assertEquals("Alpha", result["Russia"])
    }

    @Test
    fun `country with only null agencies is omitted`() {
        val players = listOf(
            player(name = "A", country = "Chile", agency = null),
            player(name = "B", country = "Chile", agency = null)
        )
        val result = Resolver(players).getMostPopularAgencyByCountry()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `goals share split by median cost for odd count`() {
        val players = listOf(
            player(name = "A", transferCost = 10, goals = 1),
            player(name = "B", transferCost = 20, goals = 1),
            player(name = "C", transferCost = 30, goals = 1),
            player(name = "D", transferCost = 40, goals = 1),
            player(name = "E", transferCost = 50, goals = 1)
        )
        val (cheapShare, expensiveShare) =
            Resolver(players).getGoalsShareByMedianCost()

        assertEquals(3.0 / 5.0, cheapShare)
        assertEquals(2.0 / 5.0, expensiveShare)
    }

    @Test
    fun `goals share for even count uses average of two middle costs`() {
        val players = listOf(
            player(name = "A", transferCost = 10, goals = 4),
            player(name = "B", transferCost = 20, goals = 0),
            player(name = "C", transferCost = 30, goals = 0),
            player(name = "D", transferCost = 40, goals = 6)
        )
        val (cheapShare, expensiveShare) =
            Resolver(players).getGoalsShareByMedianCost()

        assertEquals(4.0 / 10.0, cheapShare)
        assertEquals(6.0 / 10.0, expensiveShare)
    }

    @Test
    fun `goals shares sum to one`() {
        val players = listOf(
            player(name = "A", transferCost = 10, goals = 3),
            player(name = "B", transferCost = 20, goals = 4),
            player(name = "C", transferCost = 30, goals = 5)
        )
        val (cheap, expensive) = Resolver(players).getGoalsShareByMedianCost()
        assertEquals(1.0, cheap + expensive, absoluteTolerance = 1e-9)
    }

    @Test
    fun `empty list yields zero shares and does not crash`() {
        val (a, b) = Resolver(emptyList()).getGoalsShareByMedianCost()
        assertEquals(0.0, a)
        assertEquals(0.0, b)
    }

    @Test
    fun `zero total goals yields zero shares`() {
        val players = listOf(
            player(name = "A", transferCost = 10, goals = 0),
            player(name = "B", transferCost = 20, goals = 0)
        )
        val (a, b) = Resolver(players).getGoalsShareByMedianCost()
        assertEquals(0.0, a)
        assertEquals(0.0, b)
    }
}
