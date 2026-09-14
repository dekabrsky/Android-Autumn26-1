import model.Player
import model.Position
import model.Team
import parser.CsvParser
import resolver.Resolver
import java.io.StringReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class InfrastructureTest {

    @Test
    fun `dirty dataset is available on classpath`() {
        assertNotNull(javaClass.classLoader.getResourceAsStream("fakePlayersDirty.csv"))
    }

    @Test
    fun `clean dataset for tests is available on classpath`() {
        assertNotNull(javaClass.classLoader.getResourceAsStream("fakePlayers.csv"))
    }

    @Test
    fun `parser trims text fields recognizes position and maps empty agency to null`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
              John Smith  ;  Tigers  ;  Berlin  ;  defender  ;  Germany  ;   ; 1000 ; 10 ; 2 ; 3 ; 4 ; 1
        """.trimIndent()

        val result = CsvParser.parse(StringReader(csv))

        assertEquals(0, result.skippedCount)
        val player = result.players.single()
        assertEquals("John Smith", player.name)
        assertEquals(Team("Tigers", "Berlin"), player.team)
        assertEquals(Position.DEFENDER, player.position)
        assertEquals("Germany", player.nationality)
        assertNull(player.agency)
        assertEquals(1000L, player.transferCost)
        assertEquals(10, player.participations)
        assertEquals(2, player.goals)
        assertEquals(3, player.assists)
        assertEquals(4, player.yellowCards)
        assertEquals(1, player.redCards)
    }

    @Test
    fun `parser skips broken rows and counts them`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            Valid Player;Tigers;Berlin;FORWARD;Germany;Best Agency;1000;10;2;3;4;1
            ;Tigers;Berlin;FORWARD;Germany;Best Agency;1000;10;2;3;4;1
            Broken Position;Tigers;Berlin;COACH;Germany;Best Agency;1000;10;2;3;4;1
            Broken Number;Tigers;Berlin;FORWARD;Germany;Best Agency;oops;10;2;3;4;1
            Too Short;Tigers;Berlin;FORWARD
        """.trimIndent()

        val result = CsvParser.parse(StringReader(csv))

        assertEquals(1, result.players.size)
        assertEquals(4, result.skippedCount)
    }

    @Test
    fun `resolver returns count of players without agency`() {
        assertEquals(1, Resolver(samplePlayers()).getCountWithoutAgency())
    }

    @Test
    fun `resolver returns best scorer among defenders`() {
        assertEquals("Bob", Resolver(samplePlayers()).getBestScorerDefender().first)
        assertEquals(7, Resolver(samplePlayers()).getBestScorerDefender().second)
    }

    @Test
    fun `resolver returns russian position name of most expensive german player`() {
        assertEquals("защитник", Resolver(samplePlayers()).getTheExpensiveGermanPlayerPosition())
    }

    @Test
    fun `resolver returns team with highest average red cards`() {
        assertEquals(Team("Lions", "Madrid"), Resolver(samplePlayers()).getTheRudestTeam())
    }

    @Test
    fun `resolver returns average transfer cost by position sorted by descending average`() {
        val averages = Resolver(samplePlayers()).getAverageTransferCostByPosition()

        assertEquals(listOf(Position.FORWARD, Position.MIDFIELD, Position.DEFENDER, Position.GOALKEEPER), averages.keys.toList())
        assertEquals(400.0, averages.getValue(Position.FORWARD))
        assertEquals(400.0, averages.getValue(Position.MIDFIELD))
        assertEquals(200.0, averages.getValue(Position.DEFENDER))
        assertEquals(50.0, averages.getValue(Position.GOALKEEPER))
    }

    @Test
    fun `resolver returns top three most valuable players`() {
        val topPlayers = Resolver(samplePlayers()).getMostValuablePlayers()

        assertEquals(listOf("Aaron", "Charlie", "Dave"), topPlayers.map { it.name })
    }

    @Test
    fun `resolver returns most popular agency by country`() {
        val agencies = Resolver(samplePlayers()).getMostPopularAgencyByCountry()

        assertEquals("Agency B", agencies["Germany"])
        assertEquals("Agency A", agencies["France"])
        assertEquals("Agency C", agencies["Spain"])
    }

    @Test
    fun `resolver returns goals share split by median transfer cost`() {
        val shares = Resolver(samplePlayers()).getGoalsShareByMedianCost()

        assertEquals(1.0 / 3.0, shares.first, 0.000001)
        assertEquals(2.0 / 3.0, shares.second, 0.000001)
    }

    private fun samplePlayers(): List<Player> {
        val bears = Team("Bears", "Berlin")
        val wolves = Team("Wolves", "Paris")
        val lions = Team("Lions", "Madrid")

        return listOf(
            player("Alice", bears, Position.DEFENDER, "Germany", null, 100, goals = 5, assists = 0, redCards = 2),
            player("Bob", wolves, Position.DEFENDER, "Germany", "Agency B", 300, goals = 7, assists = 0, redCards = 0),
            player("Charlie", bears, Position.FORWARD, "France", "Agency A", 200, goals = 4, assists = 10, redCards = 1),
            player("Dave", wolves, Position.MIDFIELD, "France", "Agency A", 400, goals = 10, assists = 0, redCards = 3),
            player("Aaron", lions, Position.GOALKEEPER, "Spain", "Agency C", 50, goals = 0, assists = 12, redCards = 0),
            player("Zoe", lions, Position.FORWARD, "Spain", "Agency C", 600, goals = 1, assists = 0, redCards = 6),
        )
    }

    private fun player(
        name: String,
        team: Team,
        position: Position,
        nationality: String,
        agency: String?,
        transferCost: Long,
        goals: Int,
        assists: Int,
        redCards: Int,
    ): Player =
        Player(
            name = name,
            team = team,
            position = position,
            nationality = nationality,
            agency = agency,
            transferCost = transferCost,
            participations = 10,
            goals = goals,
            assists = assists,
            yellowCards = 0,
            redCards = redCards,
        )
}
