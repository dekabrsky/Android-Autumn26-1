import kotlin.test.Test
import kotlin.test.assertNotNull

import model.*
import parser.CsvParser
import resolver.Resolver
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
    fun `empty agency should be mapped to null`() {
        val csvLines = listOf(
            "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards",
            "Player1;TeamA;CityA;FORWARD;CountryA;;100;10;5;3;2;1"
        )

        val result = CsvParser.parse(csvLines)

        assertEquals(1, result.players.size)
        assertEquals(0, result.skippedCount)
        assertNull(result.players[0].agency)
    }

    @Test
    fun `non-empty agency should be preserved`() {
        val csvLines = listOf(
            "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards",
            "Player1;TeamA;CityA;FORWARD;CountryA;AgencyX;100;10;5;3;2;1"
        )

        val result = CsvParser.parse(csvLines)

        assertEquals(1, result.players.size)
        assertEquals(0, result.skippedCount)
        assertEquals("AgencyX", result.players[0].agency)
    }

    @Test
    fun `position should be recognized regardless of case and spaces`() {
        val csvLines = listOf(
            "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards",
            "Player1;TeamA;CityA;forward;CountryA;AgencyX;100;10;5;3;2;1",
            "Player2;TeamA;CityA;  FORWARD  ;CountryA;AgencyX;100;10;5;3;2;1",
            "Player3;TeamA;CityA;Midfield;CountryA;AgencyX;100;10;5;3;2;1",
            "Player4;TeamA;CityA;  DEFENDER  ;CountryA;AgencyX;100;10;5;3;2;1",
            "Player5;TeamA;CityA;goalkeeper;CountryA;AgencyX;100;10;5;3;2;1"
        )

        val result = CsvParser.parse(csvLines)

        assertEquals(5, result.players.size)
        assertEquals(0, result.skippedCount)
        assertEquals(Position.FORWARD, result.players[0].position)
        assertEquals(Position.FORWARD, result.players[1].position)
        assertEquals(Position.MIDFIELD, result.players[2].position)
        assertEquals(Position.DEFENDER, result.players[3].position)
        assertEquals(Position.GOALKEEPER, result.players[4].position)
    }

    @Test
    fun `empty mandatory fields should skip row`() {
        val csvLines = listOf(
            "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards",
            ";TeamA;CityA;FORWARD;CountryA;AgencyX;100;10;5;3;2;1",  // пустое имя
            "Player1;;CityA;FORWARD;CountryA;AgencyX;100;10;5;3;2;1",  // пустая команда
            "Player1;TeamA;;FORWARD;CountryA;AgencyX;100;10;5;3;2;1",  // пустой город
            "Player1;TeamA;CityA;FORWARD;;AgencyX;100;10;5;3;2;1",  // пустая страна
            "Player1;TeamA;CityA;FORWARD;CountryA;AgencyX;100;10;5;3;2;1"  // правильная строка
        )

        val result = CsvParser.parse(csvLines)

        assertEquals(1, result.players.size)
        assertEquals(4, result.skippedCount)
    }

    @Test
    fun `non-numeric values should skip row`() {
        val csvLines = listOf(
            "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards",
            "Player1;TeamA;CityA;FORWARD;CountryA;AgencyX;abc;10;5;3;2;1",  // нечисловая стоимость
            "Player2;TeamA;CityA;FORWARD;CountryA;AgencyX;100;abc;5;3;2;1",  // нечисловые участия
            "Player3;TeamA;CityA;FORWARD;CountryA;AgencyX;100;10;abc;3;2;1",  // нечисловые голы
            "Player4;TeamA;CityA;FORWARD;CountryA;AgencyX;100;10;5;abc;2;1",  // нечисловые передачи
            "Player5;TeamA;CityA;FORWARD;CountryA;AgencyX;100;10;5;3;abc;1",  // нечисловые желтые карточки
            "Player6;TeamA;CityA;FORWARD;CountryA;AgencyX;100;10;5;3;2;abc",  // нечисловые красные карточки
            "Player7;TeamA;CityA;FORWARD;CountryA;AgencyX;100;10;5;3;2;1"  // правильная строка
        )

        val result = CsvParser.parse(csvLines)

        assertEquals(1, result.players.size)
        assertEquals(6, result.skippedCount)
    }

    @Test
    fun `empty numeric values should skip row`() {
        val csvLines = listOf(
            "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards",
            "Player1;TeamA;CityA;FORWARD;CountryA;AgencyX;;10;5;3;2;1",  // пустая стоимость
            "Player2;TeamA;CityA;FORWARD;CountryA;AgencyX;100;;5;3;2;1", // пустое участие
            "Player3;TeamA;CityA;FORWARD;CountryA;AgencyX;100;10;;3;2;1", // пустые голы
            "Player4;TeamA;CityA;FORWARD;CountryA;AgencyX;100;10;5;;2;1", // пустые передачи
            "Player5;TeamA;CityA;FORWARD;CountryA;AgencyX;100;10;5;3;;1", // пустые желтые карточки
            "Player6;TeamA;CityA;FORWARD;CountryA;AgencyX;100;10;5;3;2;", // пустые красные карточки
            "Player7;TeamA;CityA;FORWARD;CountryA;AgencyX;100;10;5;3;2;1"  // правильная строка
        )

        val result = CsvParser.parse(csvLines)

        assertEquals(1, result.players.size)
        assertEquals(6, result.skippedCount)
    }

    @Test
    fun `whitespace should be trimmed from text fields`() {
        val csvLines = listOf(
            "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards",
            "  Player1  ;  TeamA  ;  CityA  ;  FORWARD  ;  CountryA  ;  AgencyX  ;100;10;5;3;2;1"
        )

        val result = CsvParser.parse(csvLines)

        assertEquals(1, result.players.size)
        assertEquals(0, result.skippedCount)
        assertEquals("Player1", result.players[0].name)
        assertEquals("TeamA", result.players[0].team.name)
        assertEquals("CityA", result.players[0].team.city)
        assertEquals("CountryA", result.players[0].country)
        assertEquals("AgencyX", result.players[0].agency)
    }

    @Test
    fun `skipped count should be correct`() {
        val csvLines = listOf(
            "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards",
            "Player1;TeamA;CityA;FORWARD;CountryA;AgencyX;100;10;5;3;2;1",  //  загружен
            "Player2;TeamA;CityA;FORWARD;CountryA;AgencyX;abc;10;5;3;2;1",  // пропущен
            "Player3;TeamA;CityA;FORWARD;CountryA;AgencyX;100;10;5;3;2;1",  // загружен
            ";TeamA;CityA;FORWARD;CountryA;AgencyX;100;10;5;3;2;1",  // пропущен
            "Player5;TeamA;CityA;FORWARD;CountryA;AgencyX;100;10;5;3;2;1"  // загружен
        )

        val result = CsvParser.parse(csvLines)

        assertEquals(3, result.players.size)
        assertEquals(2, result.skippedCount)
    }

    private fun createTestPlayers(): List<Player> {
        val teamA = Team("Team A", "City A")
        val teamB = Team("Team B", "City B")

        return listOf(
            Player(
                name = "Player1",
                team = teamA,
                position = Position.FORWARD,
                country = "Germany",
                agency = null,
                transferCost = 100,
                participations = 10,
                goals = 10,
                assists = 5,
                yellowCards = 2,
                redCards = 0
            ),
            Player(
                name = "Player2",
                team = teamA,
                position = Position.DEFENDER,
                country = "Brazil",
                agency = "AgencyX",
                transferCost = 200,
                participations = 15,
                goals = 8,
                assists = 3,
                yellowCards = 3,
                redCards = 1
            ),
            Player(
                name = "Player3",
                team = teamB,
                position = Position.DEFENDER,
                country = "Germany",
                agency = "AgencyY",
                transferCost = 300,
                participations = 20,
                goals = 15,
                assists = 7,
                yellowCards = 4,
                redCards = 2
            ),
            Player(
                name = "Player4",
                team = teamB,
                position = Position.MIDFIELD,
                country = "Germany",
                agency = null,
                transferCost = 150,
                participations = 12,
                goals = 5,
                assists = 10,
                yellowCards = 1,
                redCards = 0
            ),
            Player(
                name = "Player5",
                team = teamA,
                position = Position.FORWARD,
                country = "Brazil",
                agency = "AgencyX",
                transferCost = 250,
                participations = 18,
                goals = 20,
                assists = 12,
                yellowCards = 5,
                redCards = 1
            ),
            Player(
                name = "Player6",
                team = teamB,
                position = Position.GOALKEEPER,
                country = "Spain",
                agency = "AgencyZ",
                transferCost = 400,
                participations = 25,
                goals = 0,
                assists = 1,
                yellowCards = 0,
                redCards = 1
            )
        )
    }

    @Test
    fun `getCountWithoutAgency should return correct count`() {
        val players = createTestPlayers()
        val resolver = Resolver(players)

        assertEquals(2, resolver.getCountWithoutAgency())  // Player1 и Player4
    }

    @Test
    fun `getBestScorerDefender should return best defender`() {
        val players = createTestPlayers()
        val resolver = Resolver(players)

        val result = resolver.getBestScorerDefender()
        assertEquals("Player3", result.first)
        assertEquals(15, result.second)
    }

    @Test
    fun `getTheExpensiveGermanPlayerPosition should return correct position`() {
        val players = createTestPlayers()
        val resolver = Resolver(players)

        val result = resolver.getTheExpensiveGermanPlayerPosition()
        assertEquals("защитник", result)  // Player3 - немец, стоимость 300
    }

    @Test
    fun `getTheRudestTeam should return team with most red cards`() {
        val players = createTestPlayers()
        val resolver = Resolver(players)

        val result = resolver.getTheRudestTeam()
        assertEquals("Team B", result.name)
        assertEquals("City B", result.city)
        // Team A: Player1 (0) + Player2 (1) + Player5 (1) = 2 / 3 ≈ 0.67
        // Team B: Player3 (2) + Player4 (0) + Player6 (1) = 3 / 3 = 1.0
    }

    @Test
    fun `getAverageTransferCostByPosition should return correct averages`() {
        val players = createTestPlayers()
        val resolver = Resolver(players)

        val result = resolver.getAverageTransferCostByPosition()

        assertEquals(4, result.size)
        assertEquals(175.0, result[Position.FORWARD])  // (100 + 250) / 2
        assertEquals(250.0, result[Position.DEFENDER])  // (200 + 300) / 2
        assertEquals(150.0, result[Position.MIDFIELD])  // (150) / 1
    }

    @Test
    fun `getMostValuablePlayers should return top 3 by formula`() {
        val players = createTestPlayers()
        val resolver = Resolver(players)

        val result = resolver.getMostValuablePlayers()

        assertEquals(3, result.size)
        // Player5: 20 + 2*12 = 44
        // Player3: 15 + 2*7 = 29
        // Player4: 5 + 2*10 = 25
        assertEquals("Player5", result[0].name)
        assertEquals("Player3", result[1].name)
        assertEquals("Player4", result[2].name)
    }

    @Test
    fun `getMostValuablePlayers should sort by alphabet when same value`() {
        val players = listOf(
            createTestPlayers()[0].copy(name = "Alpha"),
            createTestPlayers()[0].copy(name = "Beta"),
            createTestPlayers()[0].copy(name = "Gamma")
        )
        val resolver = Resolver(players)

        val result = resolver.getMostValuablePlayers()

        // Все одинаковые, сортировка по алфавиту
        assertEquals("Alpha", result[0].name)
        assertEquals("Beta", result[1].name)
        assertEquals("Gamma", result[2].name)
    }

    @Test
    fun `getMostPopularAgencyByCountry should return most popular agency`() {
        val players = createTestPlayers()
        val resolver = Resolver(players)

        val result = resolver.getMostPopularAgencyByCountry()

        // Brazil: Player2 (AgencyX) и Player5 (AgencyX) → AgencyX
        assertEquals("AgencyX", result["Brazil"])
        // Germany: Player1 (null) - исключен, Player3 (AgencyY), Player4 (null) → AgencyY
        assertEquals("AgencyY", result["Germany"])
    }

    @Test
    fun `getMostPopularAgencyByCountry should filter null agencies`() {
        val players = listOf(
            createTestPlayers()[0],  // Germany, agency = null
            createTestPlayers()[2]   // Germany, agency = AgencyY
        )
        val resolver = Resolver(players)

        val result = resolver.getMostPopularAgencyByCountry()

        // Игроки с null agency не учитываются
        assertEquals(1, result.size)
        assertEquals("AgencyY", result["Germany"])
    }

    @Test
    fun `getGoalsShareByMedianCost should return correct shares`() {
        val players = createTestPlayers()
        val resolver = Resolver(players)

        val result = resolver.getGoalsShareByMedianCost()

        // Стоимости: 100, 150, 200, 250, 300
        // Медиана: 200
        // Игроки ≤ 200: Player1(10), Player2(8), Player4(5) → 23 голов
        // Игроки > 200: Player3(15), Player5(20) → 35 голов
        // Всего: 58 голов
        assertEquals(23.0 / 58.0, result.first, 0.001)   // 0.3965
        assertEquals(35.0 / 58.0, result.second, 0.001)  // 0.6034
    }

    @Test
    fun `getGoalsShareByMedianCost with empty list should return zeros`() {
        val resolver = Resolver(emptyList())

        val result = resolver.getGoalsShareByMedianCost()

        assertEquals(0.0, result.first)
        assertEquals(0.0, result.second)
    }

}
