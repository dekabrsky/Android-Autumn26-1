import model.Player
import model.Position
import model.Team
import parser.CsvParser
import resolver.Resolver
import resolver.TeamAggregate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MappingTest {

    @Test
    fun `пустое агентство превращается в null`() {
        val line = "Иван Петров;Спартак;Москва;forward;Россия;;10000000;20;5;3;2;0"
        val tempFile = createTempCsv(listOf(header(), line))

        val result = tempFile.inputStream().use {
            CsvParser.parse(it)
        }

        assertEquals(1, result.players.size)
        assertNull(result.players.first().agency)
        assertEquals(0, result.skippedCount)
    }

    @Test
    fun `битые строки пропускаются и учитываются в skippedCount`() {
        val goodLine = "Иван Петров;Спартак;Москва;forward;Россия;АгентствоА;10000000;20;5;3;2;0"
        val badLineEmptyName = ",Спартак;Москва;forward;Россия;АгентствоА;10000000;20;5;3;2;0"
        val badLineBadNumber = "Пётр Сидоров;Спартак;Москва;forward;Россия;АгентствоА;не_число;20;5;3;2;0"
        val badLinePosition = "Сергей Орлов;Спартак;Москва;неизвестная_позиция;Россия;АгентствоА;10000000;20;5;3;2;0"

        val tempFile = createTempCsv(
            listOf(
                header(),
                goodLine,
                badLineEmptyName,
                badLineBadNumber,
                badLinePosition
            )
        )

        val result = tempFile.inputStream().use {
            CsvParser.parse(it)
        }

        assertEquals(1, result.players.size)
        assertEquals(3, result.skippedCount)
    }

    @Test
    fun `позиция распознаётся в любом регистре и с пробелами`() {
        assertEquals(Position.FORWARD, Position.fromCsvTokenOrNull("  FORWARD "))
        assertEquals(Position.DEFENDER, Position.fromCsvTokenOrNull("Defender"))
        assertEquals(Position.GOALKEEPER, Position.fromCsvTokenOrNull("goalKeeper"))
        assertNull(Position.fromCsvTokenOrNull("нечто странное"))
    }

    private fun header() = "name,team_city,position,country,agency,value,matches,goals,assists,yellow,red"

    private fun createTempCsv(lines: List<String>) =
        kotlin.io.path.createTempFile(suffix = ".csv").toFile().apply {
            writeText(lines.joinToString("\n"))
        }
}

class ResolverTest {

    private val realMadrid = Team("Реал Мадрид", "Мадрид")
    private val bayern = Team("Бавария", "Мюнхен")

    private val player1 = Player(
        name = "Алексей Иванов", team = realMadrid, position = Position.DEFENDER,
        country = "Испания", agency = "СпортАгент", transferValue = 20_000_000,
        matchesPlayed = 30, goals = 5, assists = 2, yellowCards = 4, redCards = 1
    )

    private val player2 = Player(
        name = "Борис Смирнов", team = realMadrid, position = Position.DEFENDER,
        country = "Испания", agency = null, transferValue = 15_000_000,
        matchesPlayed = 28, goals = 8, assists = 1, yellowCards = 3, redCards = 2
    )

    private val player3 = Player(
        name = "Ганс Мюллер", team = bayern, position = Position.FORWARD,
        country = "Germany", agency = "СпортАгент", transferValue = 60_000_000,
        matchesPlayed = 32, goals = 25, assists = 10, yellowCards = 1, redCards = 0
    )

    private val players = listOf(player1, player2, player3)
    private val resolver = Resolver(players)

    @Test
    fun `количество игроков без агентства`() {
        assertEquals(1, resolver.countPlayersWithoutAgency())
    }

    @Test
    fun `защитник с наибольшим числом голов`() {
        val (player, goals) = resolver.topScoringDefender()!!
        assertEquals(player2, player)
        assertEquals(8, goals)
    }

    @Test
    fun `позиция самого дорогого немецкого игрока`() {
        assertEquals("нападающий", resolver.mostExpensiveGermanPlayerPositionName())
    }

    @Test
    fun `команда с наибольшим средним числом красных карточек`() {
        val team = resolver.teamWithHighestAverageRedCards()
        assertNotNull(team)
        // (1+2)/2 = 1.5 у Реала против 0/1 = 0.0 у Баварии
        assertEquals("Реал Мадрид", team.name)
    }

    @Test
    fun `пустой resolver не возвращает команду и не создаёт агрегатов`() {
        val emptyResolver = Resolver(emptyList())

        assertNull(emptyResolver.teamWithHighestAverageRedCards())
        assertTrue(emptyResolver.teamAggregates().isEmpty())
        assertTrue(emptyResolver.allTeams().isEmpty())
    }

    @Test
    fun `средняя стоимость по позициям отсортирована по убыванию`() {
        val result = resolver.averageValueByPosition()
        assertEquals(Position.FORWARD, result.first().first)
    }

    @Test
    fun `топ-3 по impact score`() {
        val top = resolver.top3ByImpactScore()
        assertEquals(player3, top.first()) // 25 + 2*10 = 45
    }

    @Test
    fun `самое популярное агентство по стране`() {
        val result = resolver.mostPopularAgencyByCountry()
        assertEquals("СпортАгент", result["Испания"])
    }

    @Test
    fun `медиана и доля голов`() {
        val report = resolver.goalsShareAroundMedianValue()
        assertEquals(20_000_000.0, report.medianValue)
        assertTrue(report.goalsSharePercentBelowMedian >= 0.0)
    }

    @Test
    fun `агрегат команды сохраняет снимок состава и вычисляет статистику без кэша`() {
        val source = mutableListOf(player1, player2)
        val aggregate = TeamAggregate(realMadrid, source)
        source.clear()

        assertEquals(2, aggregate.players.size)
        assertFailsWith<UnsupportedOperationException> {
            (aggregate.players as MutableList<Player>).clear()
        }
        assertEquals(35_000_000, aggregate.totalSquadValue)
        assertEquals(10, aggregate.totalCards)
        assertEquals(5.0, aggregate.averageCards)
        assertEquals(1.5, aggregate.averageRedCards)
    }

    @Test
    fun `пустой агрегат возвращает нулевую статистику`() {
        val aggregate = TeamAggregate(realMadrid, emptyList())

        assertEquals(0L, aggregate.totalSquadValue)
        assertEquals(0, aggregate.totalCards)
        assertEquals(0.0, aggregate.averageCards)
        assertEquals(0.0, aggregate.averageRedCards)
    }
}
