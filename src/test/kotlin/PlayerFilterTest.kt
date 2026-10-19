import filter.PlayerFilter
import model.Player
import model.Position
import model.Team
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlayerFilterTest {
    @Test
    fun `by position matches exact player position`() {
        assertTrue(PlayerFilter.ByPosition(Position.DEFENDER).matches(defender))
        assertFalse(PlayerFilter.ByPosition(Position.FORWARD).matches(defender))
    }

    @Test
    fun `cheaper than matches players with lower transfer cost`() {
        assertTrue(PlayerFilter.CheaperThan(2_000).matches(defender))
        assertFalse(PlayerFilter.CheaperThan(1_000).matches(defender))
    }

    @Test
    fun `and requires both filters to match`() {
        val filter = PlayerFilter.And(
            PlayerFilter.ByPosition(Position.DEFENDER),
            PlayerFilter.CheaperThan(2_000),
        )

        assertTrue(filter.matches(defender))
        assertFalse(filter.matches(forward))
    }

    @Test
    fun `or requires at least one filter to match`() {
        val filter = PlayerFilter.Or(
            PlayerFilter.ByPosition(Position.GOALKEEPER),
            PlayerFilter.CheaperThan(2_000),
        )

        assertTrue(filter.matches(defender))
        assertFalse(filter.matches(forward))
    }

    @Test
    fun `not inverts nested filter`() {
        val filter = PlayerFilter.Not(PlayerFilter.ByPosition(Position.DEFENDER))

        assertFalse(filter.matches(defender))
        assertTrue(filter.matches(forward))
    }

    @Test
    fun `additional filters match nationality and empty agency`() {
        assertTrue(PlayerFilter.ByNationality("Germany").matches(defender))
        assertFalse(PlayerFilter.ByNationality("Brazil").matches(defender))
        assertTrue(PlayerFilter.WithoutAgency.matches(defender))
        assertFalse(PlayerFilter.WithoutAgency.matches(forward))
    }

    private companion object {
        val team = Team("Team A", "City A")

        val defender = player(
            name = "Defender",
            position = Position.DEFENDER,
            nationality = "Germany",
            agency = null,
            transferCost = 1_000,
        )

        val forward = player(
            name = "Forward",
            position = Position.FORWARD,
            nationality = "Brazil",
            agency = "Agency A",
            transferCost = 2_000,
        )

        fun player(
            name: String,
            position: Position,
            nationality: String,
            agency: String?,
            transferCost: Long,
        ): Player = Player(
            name = name,
            team = team,
            position = position,
            nationality = nationality,
            agency = agency,
            transferCost = transferCost,
            participations = 10,
            goals = 0,
            assists = 0,
            yellowCards = 0,
            redCards = 0,
        )
    }
}
