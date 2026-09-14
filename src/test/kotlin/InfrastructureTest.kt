import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertEquals
import kotlin.test.assertNull
import parser.CsvParser
import model.Player
import model.Team
import model.Position
import java.io.File

// Пример теста: так размечаются тесты и загружаются ресурсы из classpath.
// Дополните проект тестами на CsvParser (правила маппинга) и IResolver (задачи 1–8).
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
    fun `position recognizes russian names case-insensitive`() {
        assertEquals(Position.FORWARD, Position.fromCsvTokenOrNull("нападающий"))
        assertEquals(Position.FORWARD, Position.fromCsvTokenOrNull("Нападающий"))
        assertEquals(Position.FORWARD, Position.fromCsvTokenOrNull("НАПАДАЮЩИЙ"))
        assertEquals(Position.FORWARD, Position.fromCsvTokenOrNull("  нападающий  "))

        assertEquals(Position.MIDFIELDER, Position.fromCsvTokenOrNull("полузащитник"))
        assertEquals(Position.DEFENDER, Position.fromCsvTokenOrNull("защитник"))
        assertEquals(Position.GOALKEEPER, Position.fromCsvTokenOrNull("вратарь"))
    }

    @Test
    fun `position recognizes english names case-insensitive`() {
        assertEquals(Position.FORWARD, Position.fromCsvTokenOrNull("FORWARD"))
        assertEquals(Position.FORWARD, Position.fromCsvTokenOrNull("Forward"))
        assertEquals(Position.FORWARD, Position.fromCsvTokenOrNull("forward"))

        assertEquals(Position.MIDFIELDER, Position.fromCsvTokenOrNull("MIDFIELDER"))
        assertEquals(Position.DEFENDER, Position.fromCsvTokenOrNull("defender"))
        assertEquals(Position.GOALKEEPER, Position.fromCsvTokenOrNull("Goalkeeper"))
    }

    @Test
    fun `position returns null for unknown tokens`() {
        assertNull(Position.fromCsvTokenOrNull("неизвестно"))
        assertNull(Position.fromCsvTokenOrNull("goal"))
        assertNull(Position.fromCsvTokenOrNull(""))
        assertNull(Position.fromCsvTokenOrNull("  "))
        assertNull(Position.fromCsvTokenOrNull("unknown"))
    }

    @Test
    fun `parser handles empty agency as null`() {
        val content = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            John Doe;Real Madrid;Madrid;нападающий;Spain;;1000000;10;5;3;2;0
        """.trimIndent()

        val file = File.createTempFile("test", ".csv")
        file.writeText(content)

        val parser = CsvParser()
        val result = parser.parse(file.absolutePath)

        assertEquals(1, result.players.size)
        assertEquals(0, result.skippedCount)
        assertNull(result.players.first().agency)

        file.delete()
    }

    @Test
    fun `parser treats 'null' string as null agency`() {
        val content = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            Player1;Team1;City1;нападающий;Country1;null;1000000;10;5;3;2;0
            Player2;Team2;City2;полузащитник;Country2;NULL;1000000;10;5;3;2;0
            Player3;Team3;City3;защитник;Country3;Agency;1000000;10;5;3;2;0
        """.trimIndent()

        val file = File.createTempFile("test", ".csv")
        file.writeText(content)

        val parser = CsvParser()
        val result = parser.parse(file.absolutePath)

        assertEquals(3, result.players.size)
        assertEquals(0, result.skippedCount)
        assertNull(result.players[0].agency)
        assertNull(result.players[1].agency)
        assertEquals("Agency", result.players[2].agency)

        file.delete()
    }

    @Test
    fun `parser skips row with empty name`() {
        val content = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            ;Real Madrid;Madrid;нападающий;Spain;Agency;1000000;10;5;3;2;0
            John Doe;Real Madrid;Madrid;нападающий;Spain;Agency;1000000;10;5;3;2;0
        """.trimIndent()

        val file = File.createTempFile("test", ".csv")
        file.writeText(content)

        val parser = CsvParser()
        val result = parser.parse(file.absolutePath)

        assertEquals(1, result.players.size)
        assertEquals(1, result.skippedCount)

        file.delete()
    }

    @Test
    fun `parser skips row with empty team`() {
        val content = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            John Doe;;Madrid;нападающий;Spain;Agency;1000000;10;5;3;2;0
            John Doe;Real Madrid;Madrid;нападающий;Spain;Agency;1000000;10;5;3;2;0
        """.trimIndent()

        val file = File.createTempFile("test", ".csv")
        file.writeText(content)

        val parser = CsvParser()
        val result = parser.parse(file.absolutePath)

        assertEquals(1, result.players.size)
        assertEquals(1, result.skippedCount)

        file.delete()
    }

    @Test
    fun `parser skips row with empty nationality`() {
        val content = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            John Doe;Real Madrid;Madrid;нападающий;;Agency;1000000;10;5;3;2;0
            John Doe;Real Madrid;Madrid;нападающий;Spain;Agency;1000000;10;5;3;2;0
        """.trimIndent()

        val file = File.createTempFile("test", ".csv")
        file.writeText(content)

        val parser = CsvParser()
        val result = parser.parse(file.absolutePath)

        assertEquals(1, result.players.size)
        assertEquals(1, result.skippedCount)

        file.delete()
    }

    @Test
    fun `parser skips row with empty position`() {
        val content = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            John Doe;Real Madrid;Madrid;;Spain;Agency;1000000;10;5;3;2;0
            John Doe;Real Madrid;Madrid;нападающий;Spain;Agency;1000000;10;5;3;2;0
        """.trimIndent()

        val file = File.createTempFile("test", ".csv")
        file.writeText(content)

        val parser = CsvParser()
        val result = parser.parse(file.absolutePath)

        assertEquals(1, result.players.size)
        assertEquals(1, result.skippedCount)

        file.delete()
    }

    @Test
    fun `parser skips row with invalid position`() {
        val content = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            John Doe;Real Madrid;Madrid;неизвестно;Spain;Agency;1000000;10;5;3;2;0
            John Doe;Real Madrid;Madrid;нападающий;Spain;Agency;1000000;10;5;3;2;0
        """.trimIndent()

        val file = File.createTempFile("test", ".csv")
        file.writeText(content)

        val parser = CsvParser()
        val result = parser.parse(file.absolutePath)

        assertEquals(1, result.players.size)
        assertEquals(1, result.skippedCount)

        file.delete()
    }

    @Test
    fun `parser skips row with non-numeric transfer cost`() {
        val content = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            John Doe;Real Madrid;Madrid;нападающий;Spain;Agency;не число;10;5;3;2;0
            John Doe;Real Madrid;Madrid;нападающий;Spain;Agency;1000000;10;5;3;2;0
        """.trimIndent()

        val file = File.createTempFile("test", ".csv")
        file.writeText(content)

        val parser = CsvParser()
        val result = parser.parse(file.absolutePath)

        assertEquals(1, result.players.size)
        assertEquals(1, result.skippedCount)

        file.delete()
    }

    @Test
    fun `parser skips row with non-numeric participations`() {
        val content = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            John Doe;Real Madrid;Madrid;нападающий;Spain;Agency;1000000;не число;5;3;2;0
            John Doe;Real Madrid;Madrid;нападающий;Spain;Agency;1000000;10;5;3;2;0
        """.trimIndent()

        val file = File.createTempFile("test", ".csv")
        file.writeText(content)

        val parser = CsvParser()
        val result = parser.parse(file.absolutePath)

        assertEquals(1, result.players.size)
        assertEquals(1, result.skippedCount)

        file.delete()
    }

    @Test
    fun `parser trims whitespace from fields`() {
        val content = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            John Doe ; Real Madrid ; Madrid ; нападающий ; Spain ; Agency ; 1000000 ; 10 ; 5 ; 3 ; 2 ; 0
        """.trimIndent()

        val file = File.createTempFile("test", ".csv")
        file.writeText(content)

        val parser = CsvParser()
        val result = parser.parse(file.absolutePath)

        assertEquals(1, result.players.size)
        assertEquals(0, result.skippedCount)
        assertEquals("John Doe", result.players.first().name)
        assertEquals("Real Madrid", result.players.first().team.name)

        file.delete()
    }

    private fun createTestPlayers(): List<Player> {
        val team1 = Team("Real Madrid", "Madrid")
        val team2 = Team("Barcelona", "Barcelona")
        val team3 = Team("Bayern", "Munich")

        return listOf(
            Player(
                name = "Player1",
                team = team1,
                city = "Madrid",
                position = Position.DEFENDER,
                nationality = "Spain",
                agency = null,
                transferCost = 1000000,
                participation = 10,
                goals = 5,
                assists = 2,
                yellowCards = 3,
                redCards = 0
            ),
            Player(
                name = "Player2",
                team = team1,
                city = "Madrid",
                position = Position.DEFENDER,
                nationality = "Germany",
                agency = "AgencyA",
                transferCost = 5000000,
                participation = 20,
                goals = 8,
                assists = 3,
                yellowCards = 5,
                redCards = 1
            ),
            Player(
                name = "Player3",
                team = team2,
                city = "Barcelona",
                position = Position.FORWARD,
                nationality = "Spain",
                agency = "AgencyB",
                transferCost = 3000000,
                participation = 15,
                goals = 10,
                assists = 4,
                yellowCards = 2,
                redCards = 0
            ),
            Player(
                name = "Player4",
                team = team2,
                city = "Barcelona",
                position = Position.FORWARD,
                nationality = "Germany",
                agency = "AgencyA",
                transferCost = 8000000,
                participation = 25,
                goals = 15,
                assists = 5,
                yellowCards = 1,
                redCards = 0
            ),
            Player(
                name = "Player5",
                team = team3,
                city = "Munich",
                position = Position.MIDFIELDER,
                nationality = "Germany",
                agency = null,
                transferCost = 4000000,
                participation = 18,
                goals = 3,
                assists = 6,
                yellowCards = 4,
                redCards = 2
            ),
            Player(
                name = "Player6",
                team = team3,
                city = "Munich",
                position = Position.GOALKEEPER,
                nationality = "Spain",
                agency = "AgencyC",
                transferCost = 2000000,
                participation = 12,
                goals = 0,
                assists = 1,
                yellowCards = 0,
                redCards = 0
            )
        )
    }

    @Test
    fun `resolver returns count without agency`() {
        val resolver = Resolver(createTestPlayers())
        assertEquals(2, resolver.getCountWithoutAgency())
    }

    @Test
    fun `resolver finds best scoring defender`() {
        val resolver = Resolver(createTestPlayers())
        val (name, goals) = resolver.getBestScorerDefender()
        assertEquals("Player2", name)
        assertEquals(8, goals)
    }

    @Test
    fun `resolver returns empty for best scoring defender when no defenders`() {
        val noDefenders = createTestPlayers().filter { it.position != Position.DEFENDER }
        val resolver = Resolver(noDefenders)
        val (name, goals) = resolver.getBestScorerDefender()
        assertEquals("", name)
        assertEquals(0, goals)
    }

    @Test
    fun `resolver finds most expensive german player position`() {
        val resolver = Resolver(createTestPlayers())
        assertEquals("нападающий", resolver.getTheExpensiveGermanPlayerPosition())
    }

    @Test
    fun `resolver returns not found for most expensive german when no germans`() {
        val noGermans = createTestPlayers().filter { it.nationality!= "Germany" }
        val resolver = Resolver(noGermans)
        assertEquals("не найдено", resolver.getTheExpensiveGermanPlayerPosition())
    }

    @Test
    fun `resolver finds rudest team`() {
        val resolver = Resolver(createTestPlayers())
        val team = resolver.getTheRudestTeam()
        assertEquals("Bayern", team.name)
        assertEquals("Munich", team.city)
    }

    @Test
    fun `resolver calculates average transfer cost by position`() {
        val resolver = Resolver(createTestPlayers())
        val averages = resolver.getAverageTransferCostByPosition()

        assertEquals(5500000.0, averages[Position.FORWARD])
        assertEquals(3000000.0, averages[Position.DEFENDER])
        assertEquals(4000000.0, averages[Position.MIDFIELDER])
        assertEquals(2000000.0, averages[Position.GOALKEEPER])
    }

    @Test
    fun `resolver returns top 3 most valuable players`() {
        val resolver = Resolver(createTestPlayers())
        val top = resolver.getMostValuablePlayers()

        assertEquals(3, top.size)
        assertEquals("Player4", top[0].name)
        assertEquals("Player3", top[1].name)
        assertEquals("Player5", top[2].name)
    }

    @Test
    fun `resolver finds most popular agency by country`() {
        val resolver = Resolver(createTestPlayers())
        val agencies = resolver.getMostPopularAgencyByCountry()

        assertEquals(2, agencies.size)
        assertEquals("AgencyA", agencies["Germany"])
        assertEquals("AgencyB", agencies["Spain"])
    }

    @Test
    fun `resolver calculates goals share by median cost`() {
        val resolver = Resolver(createTestPlayers())
        val (below, above) = resolver.getGoalsShareByMedianCost()
        assertEquals(15.0 / 41.0, below, 0.01)
        assertEquals(26.0 / 41.0, above, 0.01)
    }

    @Test
    fun `resolver handles empty list for goals share`() {
        val resolver = Resolver(emptyList())
        val (below, above) = resolver.getGoalsShareByMedianCost()
        assertEquals(0.0, below)
        assertEquals(0.0, above)
    }

    @Test
    fun `resolver handles single player for goals share`() {
        val team = Team("Team", "City")
        val singlePlayer = listOf(
            Player(
                name = "Player",
                team = team,
                city = "City",
                position = Position.FORWARD,
                nationality = "Spain",
                agency = null,
                transferCost = 5000000,
                participation = 10,
                goals = 10,
                assists = 0,
                yellowCards = 0,
                redCards = 0
            )
        )
        val resolver = Resolver(singlePlayer)
        val (below, above) = resolver.getGoalsShareByMedianCost()
        assertEquals(1.0, below)
        assertEquals(0.0, above)
    }
}
