import model.Player
import model.Position
import model.Team
import visualization.calculateTopTeamCostShares
import kotlin.test.Test
import kotlin.test.assertEquals

class VisualizationDataTest {

    private fun player(
        name: String,
        team: Team,
        cost: Long
    ): Player {
        return Player(
            name = name,
            team = team,
            position = Position.FORWARD,
            country = "Germany",
            agency = null,
            transferCost = cost,
            matches = 10,
            goals = 0,
            assists = 0,
            yellowCards = 0,
            redCards = 0
        )
    }

    @Test
    fun `calculates top teams and cumulative shares`() {
        val firstTeam = Team(
            name = "First",
            city = "A"
        )

        val secondTeam = Team(
            name = "Second",
            city = "B"
        )

        val thirdTeam = Team(
            name = "Third",
            city = "C"
        )

        val players = listOf(
            player(
                name = "A",
                team = firstTeam,
                cost = 40L
            ),
            player(
                name = "B",
                team = firstTeam,
                cost = 20L
            ),
            player(
                name = "C",
                team = secondTeam,
                cost = 30L
            ),
            player(
                name = "D",
                team = thirdTeam,
                cost = 10L
            )
        )

        val result = calculateTopTeamCostShares(
            players = players,
            limit = 2
        )

        assertEquals(
            listOf("First", "Second"),
            result.map { it.team.name }
        )

        // First = 60 из общей стоимости 100.
        assertEquals(
            expected = 0.6,
            actual = result[0].cumulativeShare,
            absoluteTolerance = 0.000001
        )

        // First + Second = 90 из общей стоимости 100.
        assertEquals(
            expected = 0.9,
            actual = result[1].cumulativeShare,
            absoluteTolerance = 0.000001
        )
    }
}