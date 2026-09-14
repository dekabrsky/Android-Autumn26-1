import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import model.*
import resolver.Resolver

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

    //маппинг

    @Test
    fun `position parsing is case-insensitive`() {
        assertEquals(Position.FORWARD, Position.fromCsvTokenOrNull("forward"))
        assertEquals(Position.MIDFIELD, Position.fromCsvTokenOrNull("  MIDFIELD  "))
        assertEquals(Position.DEFENDER, Position.fromCsvTokenOrNull("Defender"))
        assertEquals(Position.GOALKEEPER, Position.fromCsvTokenOrNull("goalkeeper"))
        assertNull(Position.fromCsvTokenOrNull("unknown"))
        assertNull(Position.fromCsvTokenOrNull(""))
    }

    @Test
    fun `empty agency is null`() {
        val p = Player("A", Team("T", "C"), Position.FORWARD, "RU", null, 100, 1, 0, 0, 0, 0)
        assertNull(p.agency)
    }

    //Resolver

    private val players = listOf(
        Player("Alice", Team("TeamA", "CityA"), Position.FORWARD, "Germany", "Ag1", 5_000_000, 20, 15, 5, 2, 0),
        Player("Bob",   Team("TeamA", "CityA"), Position.DEFENDER, "Germany", "Ag1", 3_000_000, 25, 2, 1, 5, 1),
        Player("Charlie", Team("TeamB", "CityB"), Position.MIDFIELD, "Brazil", null, 4_000_000, 22, 8, 10, 3, 0),
        Player("David", Team("TeamB", "CityB"), Position.FORWARD, "Brazil", "Ag2", 6_000_000, 18, 12, 4, 1, 0),
        Player("Eve",   Team("TeamC", "CityC"), Position.DEFENDER, "Germany", null, 2_000_000, 30, 1, 2, 8, 2)
    )

    private val r = Resolver(players)

    @Test fun `1 - Игроков без агентства`() = assertEquals(2, r.getCountWithoutAgency())

    @Test fun `2 - Лучший бомбардир-защитник`() {
        val (name, goals) = r.getBestScorerDefender()
        assertEquals("Bob", name); assertEquals(2, goals)
    }

    @Test fun `3 - Позиция самого дорогого немецкого игрока`() =
        assertEquals("нападающий", r.getTheExpensiveGermanPlayerPosition())

    @Test fun `4 - Самая грубая команда`() = assertEquals("TeamC", r.getTheRudestTeam().name)

    @Test fun `5 - Средняя стоимость по позициям`() {
        val avg = r.getAverageTransferCostByPosition()
        assertEquals(3, avg.size)
        assertEquals(5_500_000.0, avg[Position.FORWARD]!!, 0.01)
    }

    @Test fun `6 - топ-3 самых полезных игрока`() {
        val top = r.getMostValuablePlayers()
        assertEquals(3, top.size)
        assertEquals("Charlie", top[0].name)  // 28 очков
        assertEquals("Alice", top[1].name)    // 25 очков
    }

    @Test fun `7 - Самые популярные агентства по странам`() {
        val map = r.getMostPopularAgencyByCountry()
        assertTrue(map.containsKey("Germany"))
        assertTrue(map.containsKey("Brazil"))
    }

    @Test fun `8 - Доли голов относительно медианы`() {
        val (a, b) = r.getGoalsShareByMedianCost()
        assertTrue(a in 0.0..1.0 && b in 0.0..1.0)
        assertEquals(1.0, a + b, 0.01)
    }
}
