import model.Player
import model.Position
import model.Team
import parser.CsvParser
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

// Пример теста: так размечаются тесты и загружаются ресурсы из classpath.
// Дополните проект тестами на CsvParser (правила маппинга) и IResolver (задачи 1–8).
class InfrastructureTest {

    @Test
    fun `should parse empty agency as null and skip invalid rows`() {
        val testCsv = """
Оглавление
Name;  Team;City  ;DEFENDER;Brazil;D'Amore LLC;12345;6;7;8;9;10
Name;Team;City;DEFENDER;Brazil;;12345;6;7;8;9;10
Name;Team;City;DEFENDER;Brazil;   ;12345;6;7;8;9;10
Name;Team;City;DEFENDER;Brazil;12345;6;7;8;9;10;
Name;Team;City;DEfeNDEr;Brazil;;12345;6;7;8;9;10
Name;Team;City;defender;Brazil;;12345;6;7;8;9;10

;Team;City;DEFENDER;Brazil;;12345;6;7;8;9;10
Name;Team;;DEFENDER;Brazil;;12345;6;7;8;9;10
    """.trimIndent()

        val result = CsvParser.parse(testCsv.byteInputStream())

        assertEquals(4, result.skippedCount)
        assertEquals(5, result.players.size)

        assertEquals("D'Amore LLC", result.players[0].agency)
        assertEquals(null, result.players[1].agency)
        assertEquals(null, result.players[2].agency)

        assertEquals(Position.DEFENDER, result.players[3].position)
        assertEquals(Position.DEFENDER.russianName, result.players[3].position.russianName)
        assertEquals(Position.DEFENDER, result.players[4].position)
        assertEquals(Position.DEFENDER.russianName, result.players[4].position.russianName)
    }

    @Test
    fun `dirty dataset is available on classpath`() {
        assertNotNull(javaClass.classLoader.getResourceAsStream("fakePlayersDirty.csv"))
    }

    @Test
    fun `clean dataset for tests is available on classpath`() {
        assertNotNull(javaClass.classLoader.getResourceAsStream("fakePlayers.csv"))
    }
}

class ResolverTest {

    // Тестовые команды
    private val testTeams = listOf(
        Team(
            "Test FC 1",
            "Test City 1",
            3
        ),
        Team(
            "Test FC 2",
            "Test City 2",
            1
        )
    )

    // Тестовые игроки
    private val testPlayers = listOf(
        Player(
            "Player 1",
            testTeams[0],
            Position.FORWARD,
            "Brazil",
            "Some Agency",
            1000,
            10,
            5,
            2,
            0,
            3
        ),
        Player(
            "Player 2",
            testTeams[0],
            Position.DEFENDER,
            "Germany",
            null,
            2000,
            15,
            1,
            0,
            1,
            2
        ),
        Player(
            "Player 3",
            testTeams[0],
            Position.DEFENDER,
            "Germany",
            null,
            20_000,
            25,
            10,
            0,
            1,
            0
        ),
        Player(
            "Player 4",
            testTeams[1],
            Position.GOALKEEPER,
            "Russia",
            "Some Agency 1",
            60_000_000,
            30,
            1,
            0,
            0,
            0
        ),
    )

    private val resolver = Resolver(testPlayers)

    // getCountWithoutAgency()
    @Test
    fun `should return correct count without agency`() {
        assertEquals(2, resolver.getCountWithoutAgency())
    }

    // getBestScorerDefender()
    @Test
    fun `should return the highest-scoring defender`() {
        assertEquals("Player 3" to 10, resolver.getBestScorerDefender())
    }

    // getBestScorerDefenderWithFilter()
    @Test
    fun `should return the highest-scoring defender with filter`() {
        assertEquals("Player 3" to 10, resolver.getBestScorerDefenderWithFilter())
    }

    // getTheExpensiveGermanPlayerPosition()
    @Test
    fun `should return the position of the most expensive German player`() {
        assertEquals("Защитник", resolver.getTheExpensiveGermanPlayerPosition())
    }

    // getTheRudestTeam()
    @Test
    fun `should return the team with the highest average number of the red cards`() {
        assertEquals(
            Team("Test FC 1", "Test City 1", 3),
            resolver.getTheRudestTeam())
    }

    // getAverageTransferCostByPosition()
    @Test
    fun `should return the average transfer cost of player by positions`() {
        assertEquals(
            mapOf(
                Position.DEFENDER to 11_000.0,
                Position.GOALKEEPER to 60_000_000.0,
                Position.FORWARD to 1_000.0,
            ),
            resolver.getAverageTransferCostByPosition()
        )
    }

    // getMostValuablePlayers()
    @Test
    fun `should return top 3 the most highest-scoring players`() {
        assertEquals(
            listOf(
                Player("Player 3", testTeams[0], Position.DEFENDER, "Germany", null, 20_000, 25, 10, 0, 1, 0),
                Player("Player 1", testTeams[0], Position.FORWARD, "Brazil", "Some Agency", 1000, 10, 5, 2, 0, 3),
                Player("Player 2", testTeams[0], Position.DEFENDER, "Germany", null, 2000, 15, 1, 0, 1, 2),
            ),
            resolver.getMostValuablePlayers()
        )
    }

    // getMostPopularAgencyByCountry
    @Test
    fun `should return the most popular agencies by country`() {
        assertEquals(
            mapOf(
                "Brazil" to "Some Agency",
                "Germany" to "Агентств нет",
                "Russia" to "Some Agency 1"
            ),
            resolver.getMostPopularAgencyByCountry()
        )
    }

    // getGoalsShareByMedianCost()
    @Test
    fun `should return the share of goals scored by players with transfer values below and above the median`() {
        assertEquals(
            0.35294117647058826 to 0.6470588235294118,
            resolver.getGoalsShareByMedianCost()
        )
    }

    //getGoalsShareByMedianCostWithFilter()
    @Test
    fun `should return the share of goals scored by players with transfer values below and above the median with filter`() {
        assertEquals(
            0.35294117647058826 to 0.6470588235294118,
            resolver.getGoalsShareByMedianCostWithFilter()
        )
    }
}
