import model.Player
import model.Position
import model.Team
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ResolverTest {

    private fun player(
        name: String,
        team: Team,
        position: Position,
        nationality: String = "Brazil",
        agency: String? = "Default Agency",
        transferCost: Long = 1_000_000,
        participations: Int = 10,
        goals: Int = 0,
        assists: Int = 0,
        yellowCards: Int = 0,
        redCards: Int = 0
    ): Player {
        val created = Player(
            name = name,
            team = team,
            position = position,
            nationality = nationality,
            agency = agency,
            transferCost = transferCost,
            participations = participations,
            goals = goals,
            assists = assists,
            yellowCards = yellowCards,
            redCards = redCards
        )
        team.addPlayer(created)
        return created
    }

    @Test
    fun `counts players without agency`() {
        val team = Team("Team A", "City A")
        val players = listOf(
            player("P1", team, Position.FORWARD, agency = null),
            player("P2", team, Position.DEFENDER, agency = "Agency X"),
            player("P3", team, Position.MIDFIELD, agency = null)
        )

        assertEquals(2, Resolver(players).getCountWithoutAgency())
    }

    @Test
    fun `finds best scoring defender ignoring other positions`() {
        val team = Team("Team A", "City A")
        val players = listOf(
            player("Def1", team, Position.DEFENDER, goals = 3),
            player("Def2", team, Position.DEFENDER, goals = 7),
            player("Fwd1", team, Position.FORWARD, goals = 100)
        )

        val (name, goals) = Resolver(players).getBestScorerDefender()

        assertEquals("Def2", name)
        assertEquals(7, goals)
    }

    @Test
    fun `throws when there are no defenders`() {
        val team = Team("Team A", "City A")
        val players = listOf(player("F1", team, Position.FORWARD))

        assertFailsWith<NoSuchElementException> {
            Resolver(players).getBestScorerDefender()
        }
    }

    @Test
    fun `finds position of the most expensive german player`() {
        val team = Team("Team A", "City A")
        val players = listOf(
            player("G1", team, Position.MIDFIELD, nationality = "Germany", transferCost = 5_000_000),
            player("G2", team, Position.GOALKEEPER, nationality = "Germany", transferCost = 9_000_000),
            player("Other", team, Position.FORWARD, nationality = "Brazil", transferCost = 20_000_000)
        )

        assertEquals(
            Position.GOALKEEPER.positionName,
            Resolver(players).getTheExpensiveGermanPlayerPosition()
        )
    }

    @Test
    fun `finds team with highest average red cards per player`() {
        val teamA = Team("Team A", "City A")
        val teamB = Team("Team B", "City B")

        val players = listOf(
            player("A1", teamA, Position.DEFENDER, redCards = 2),
            player("A2", teamA, Position.FORWARD, redCards = 4),
            player("B1", teamB, Position.MIDFIELD, redCards = 9)
        )

        assertEquals("Team B", Resolver(players).getTheRudestTeam().name)
    }

    @Test
    fun `computes average transfer cost per position sorted descending`() {
        val team = Team("Team A", "City A")
        val players = listOf(
            player("D1", team, Position.DEFENDER, transferCost = 10_000_000),
            player("D2", team, Position.DEFENDER, transferCost = 30_000_000),
            player("F1", team, Position.FORWARD, transferCost = 50_000_000)
        )

        val result = Resolver(players).getAverageTransferCostByPosition()

        assertEquals(listOf(Position.FORWARD, Position.DEFENDER), result.keys.toList())
        assertEquals(50_000_000.0, result[Position.FORWARD])
        assertEquals(20_000_000.0, result[Position.DEFENDER])
    }

    @Test
    fun `returns top three players with alphabetical tiebreak on equal usefulness`() {
        val team = Team("Team A", "City A")
        val players = listOf(
            player("Zack", team, Position.FORWARD, goals = 5, assists = 5),
            player("Anna", team, Position.MIDFIELD, goals = 5, assists = 5),
            player("Mia", team, Position.DEFENDER, goals = 10, assists = 0),
            player("Bob", team, Position.FORWARD, goals = 0, assists = 0)
        )

        val top3 = Resolver(players).getMostValuablePlayers().map { it.name }

        assertEquals(listOf("Anna", "Zack", "Mia"), top3)
    }

    @Test
    fun `finds most popular agency per country`() {
        val team = Team("Team A", "City A")
        val players = listOf(
            player("P1", team, Position.FORWARD, nationality = "Brazil", agency = "Zeta Agency"),
            player("P2", team, Position.MIDFIELD, nationality = "Brazil", agency = "Alpha Agency"),
            player("P3", team, Position.DEFENDER, nationality = "Brazil", agency = "Alpha Agency"),
            player("P4", team, Position.GOALKEEPER, nationality = "Germany", agency = null)
        )

        val result = Resolver(players).getMostPopularAgencyByCountry()

        assertEquals("Alpha Agency", result["Brazil"])
        assertEquals(false, result.containsKey("Germany"))
    }

    @Test
    fun `picks alphabetically first agency when counts are tied`() {
        val team = Team("Team A", "City A")
        val players = listOf(
            player("P1", team, Position.FORWARD, nationality = "Brazil", agency = "Zeta Agency"),
            player("P2", team, Position.MIDFIELD, nationality = "Brazil", agency = "Alpha Agency")
        )

        assertEquals("Alpha Agency", Resolver(players).getMostPopularAgencyByCountry()["Brazil"])
    }

    @Test
    fun `splits goals share by median cost with even player count`() {
        val team = Team("Team A", "City A")
        val players = listOf(
            player("P1", team, Position.FORWARD, transferCost = 10, goals = 2),
            player("P2", team, Position.MIDFIELD, transferCost = 20, goals = 3),
            player("P3", team, Position.DEFENDER, transferCost = 30, goals = 5),
            player("P4", team, Position.GOALKEEPER, transferCost = 40, goals = 10)
        )

        val (cheapShare, expensiveShare) = Resolver(players).getGoalsShareByMedianCost()

        assertEquals(0.25, cheapShare, 0.0001)
        assertEquals(0.75, expensiveShare, 0.0001)
    }

    @Test
    fun `splits goals share by median cost with odd player count`() {
        val team = Team("Team A", "City A")
        val players = listOf(
            player("P1", team, Position.FORWARD, transferCost = 10, goals = 1),
            player("P2", team, Position.MIDFIELD, transferCost = 20, goals = 2),
            player("P3", team, Position.DEFENDER, transferCost = 30, goals = 3)
        )

        val (cheapShare, expensiveShare) = Resolver(players).getGoalsShareByMedianCost()

        assertEquals(0.5, cheapShare, 0.0001)
        assertEquals(0.5, expensiveShare, 0.0001)
    }

    @Test
    fun `returns zero shares when there are no goals at all`() {
        val team = Team("Team A", "City A")
        val players = listOf(
            player("P1", team, Position.FORWARD, transferCost = 10, goals = 0),
            player("P2", team, Position.MIDFIELD, transferCost = 20, goals = 0)
        )

        val (cheapShare, expensiveShare) = Resolver(players).getGoalsShareByMedianCost()

        assertEquals(0.0, cheapShare)
        assertEquals(0.0, expensiveShare)
    }
}