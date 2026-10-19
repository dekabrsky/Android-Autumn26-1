import model.Player
import model.Position
import model.Team
import visualization.calculateCountryShares
import visualization.calculateTopTeamCostShares
import kotlin.test.Test
import kotlin.test.assertEquals

class VisualizationDataTest {
    @Test
    fun `country shares are calculated and sorted by player count`() {
        val shares = calculateCountryShares(players)

        assertEquals(
            listOf("Brazil", "Germany", "Croatia"),
            shares.map { it.country },
        )
        assertEquals(listOf(3, 2, 1), shares.map { it.playersCount })
        assertEquals(listOf(0.5, 1.0 / 3.0, 1.0 / 6.0), shares.map { it.share })
    }

    @Test
    fun `top team cost shares are calculated with cumulative share`() {
        val teamShares = calculateTopTeamCostShares(players, limit = 2)

        assertEquals(listOf(teamB, teamA), teamShares.map { it.team })
        assertEquals(listOf(700L, 300L), teamShares.map { it.totalCost })
        assertEquals(listOf(700.0 / 1_200.0, 1_000.0 / 1_200.0), teamShares.map { it.cumulativeShare })
    }

    private companion object {
        val teamA = Team("Team A", "City A")
        val teamB = Team("Team B", "City B")
        val teamC = Team("Team C", "City C")

        val players = listOf(
            player("Alice", teamA, "Germany", 100),
            player("Bob", teamA, "Brazil", 200),
            player("Carol", teamB, "Brazil", 300),
            player("Dave", teamB, "Croatia", 400),
            player("Eve", teamC, "Germany", 50),
            player("Frank", teamC, "Brazil", 150),
        )

        fun player(name: String, team: Team, nationality: String, transferCost: Long): Player = Player(
            name = name,
            team = team,
            position = Position.FORWARD,
            nationality = nationality,
            agency = null,
            transferCost = transferCost,
            participations = 10,
            goals = 0,
            assists = 0,
            yellowCards = 0,
            redCards = 0,
        )
    }
}
