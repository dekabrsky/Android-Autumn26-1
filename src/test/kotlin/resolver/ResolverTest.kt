package resolver

import model.Player
import model.Position
import model.Team
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolverTest {
    private fun fixture(): Pair<Resolver, List<Player>> {
        val teamA = Team("Team A", "City A")
        val teamB = Team("Team B", "City B")
        val players = listOf(
            player("Alice", teamA, Position.DEFENDER, "Germany", "Zulu", 100, goals = 5, assists = 1, red = 2),
            player("Bob", teamA, Position.FORWARD, "Spain", null, 300, goals = 1, assists = 4, red = 0),
            player("Carol", teamB, Position.DEFENDER, "Germany", "Alpha", 200, goals = 7, red = 3),
            player("Aaron", teamB, Position.MIDFIELD, "France", "Beta", 400, red = 3),
        )
        return Resolver(players) to players
    }

    @Test
    fun `query 1 counts players without agency`() {
        assertEquals(1, fixture().first.getCountWithoutAgency())
    }

    @Test
    fun `query 2 returns highest scoring defender`() {
        assertEquals("Carol" to 7, fixture().first.getBestScorerDefender())
    }

    @Test
    fun `query 3 returns russian position of most expensive german`() {
        assertEquals("защитник", fixture().first.getTheExpensiveGermanPlayerPosition())
    }

    @Test
    fun `query 4 returns team with greatest average red card count`() {
        assertEquals("Team B", fixture().first.getTheRudestTeam().name)
    }

    @Test
    fun `query 5 returns position averages in descending order`() {
        val result = fixture().first.getAverageTransferCostByPosition()

        assertEquals(listOf(Position.MIDFIELD, Position.FORWARD, Position.DEFENDER), result.keys.toList())
        assertEquals(150.0, result.getValue(Position.DEFENDER))
    }

    @Test
    fun `query 6 sorts by usefulness and then by name`() {
        val result = fixture().first.getMostValuablePlayers().map { it.name }

        assertEquals(listOf("Bob", "Alice", "Carol"), result)
    }

    @Test
    fun `query 7 selects popular agency with alphabetical tie break`() {
        val result = fixture().first.getMostPopularAgencyByCountry()

        assertEquals(mapOf("France" to "Beta", "Germany" to "Alpha"), result)
    }

    @Test
    fun `query 8 calculates goal shares around even median`() {
        val result = fixture().first.getGoalsShareByMedianCost()

        assertEquals(12.0 / 13.0, result.first, 1e-9)
        assertEquals(1.0 / 13.0, result.second, 1e-9)
    }

    @Test
    fun `zero total goals produces zero shares`() {
        val team = Team("Team", "City")
        val resolver = Resolver(listOf(player("A", team, cost = 100)))

        assertEquals(0.0 to 0.0, resolver.getGoalsShareByMedianCost())
    }

    private fun player(
        name: String,
        team: Team,
        position: Position = Position.FORWARD,
        country: String = "Germany",
        agency: String? = "Agency",
        cost: Int,
        goals: Int = 0,
        assists: Int = 0,
        red: Int = 0,
    ) = Player(
        name = name,
        team = team,
        position = position,
        country = country,
        agency = agency,
        transferCost = cost,
        gamesCount = 10,
        goalsCount = goals,
        goalPassesCount = assists,
        yellowCardsCount = 0,
        redCardsCount = red,
    )
}
