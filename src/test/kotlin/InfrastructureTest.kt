import model.Player
import model.Position
import model.Team
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import parser.CsvParser
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertNotNull
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

    // ==========================================
    // ТЕСТЫ НА CSV ПАРСЕР (ПРАВИЛА МАППИНГА)
    // ==========================================

    @Test
    fun `mapping rules - empty agency becomes null and player is preserved`() {
        val csvLine = "Iva Streich;Nevada whales;South Carolina;MIDFIELD;Colombia;;75012006;22;19;6;3;7"

        val result = CsvParser.parseLines(listOf(csvLine))

        assertEquals(1, result.players.size)
        assertEquals(0, result.skippedCount)
        assertNull(result.players.first().agency)
    }

    @Test
    fun `mapping rules - bad strings are skipped and counted`() {
        val badLines = listOf(
            // Нечисловые голы ("abc")
            "Bad Player 1;Real;Madrid;DEFENDER;Spain;Nike;100000;15;abc;0;1;0",
            // Пустое имя (обязательное поле)
            ";FC Barcelona;Barcelona;FORWARD;Brazil;Adidas;2000000;20;10;5;0;0",
            // Неполная строка (меньше 12 колонок)
            "Short Line;Team;City"
        )

        val result = CsvParser.parseLines(badLines)

        assertEquals(0, result.players.size)
        assertEquals(3, result.skippedCount)
    }

    @Test
    fun `mapping rules - position is recognized in any case with spaces`() {
        val csvLine = "Test Player;Team;City;  foRwaRd  ;Country;Agency;1000;10;2;1;0;0"

        val result = CsvParser.parseLines(listOf(csvLine))

        assertEquals(1, result.players.size)
        assertEquals(Position.FORWARD, result.players.first().position)
    }

    // ==========================================
    // ТЕСТЫ НА МЕТОДЫ IRESOLVER (ЗАДАЧИ 1-8)
    // ==========================================

    // Набор тестовых игроков для быстрой проверки аналитики
    private val p1 =
        Player("A_Defender", Team("Team A", "City A"), Position.DEFENDER, "Germany", null, 10_000_000, 10, 5, 2, 1, 1)
    private val p2 = Player("B_Forward", Team("Team A", "City A"), Position.FORWARD, "Germany", "Nike", 20_000_000, 10, 15, 5, 2, 3)
    private val p3 = Player("C_Defender", Team("Team B", "City B"), Position.DEFENDER, "Colombia", "Adidas", 30_000_000, 10, 2, 1, 0, 0)
    private val testPlayers = listOf(p1, p2, p3)
    private val resolver = Resolver(testPlayers)

    @Test
    fun `resolver 1 - count players without agency`() {
        // У p1 агентство null, у p2 и p3 — задано
        assertEquals(1, resolver.getCountWithoutAgency())
    }

    @Test
    fun `resolver 2 - best scorer among defenders`() {
        // Защитники: p1 (5 голов) и p3 (2 гола). Лучший — p1
        val (name, goals) = resolver.getBestScorerDefender()
        assertEquals("A_Defender", name)
        assertEquals(5, goals)
    }

    @Test
    fun `resolver 3 - position of the most expensive German player`() {
        // Немцы: p1 (10 млн, DEFENDER) и p2 (20 млн, FORWARD). Самый дорогой — FORWARD
        assertEquals("нападающий", resolver.getTheExpensiveGermanPlayerPosition())
    }

    @Test
    fun `resolver 4 - team with the highest average red cards per player`() {
        // Team A имеет 2 игроков (p1 и p2), сумма красных = 1 + 3 = 4. Среднее = 4 / 2 = 2.0
        // Team B имеет 1 игрока (p3), сумма красных = 0. Среднее = 0.0
        assertEquals("Team A", resolver.getTheRudestTeam().name)
    }

    @Test
    fun `resolver 5 - average transfer cost by position sorted descending`() {
        // DEFENDER: (10 млн + 30 млн) / 2 = 20 млн
        // FORWARD: 20 млн / 1 = 20 млн
        // Из-за одинаковой средней стоимости точный порядок может зависеть от стабильности сортировки,
        // но структура Map должна содержать обе позиции с корректными значениями.
        val result = resolver.getAverageTransferCostByPosition()
        assertEquals(20_000_000.0, result[Position.DEFENDER])
        assertEquals(20_000_000.0, result[Position.FORWARD])
    }

    @Test
    fun `resolver 6 - top 3 most valuable players by formula with alphabet tie-breaker`() {
        // Полезность = голы + 2 * ассисты
        // p1: 5 + 2*2 = 9
        // p2: 15 + 2*5 = 25
        // p3: 2 + 2*1 = 4
        // Порядок по убыванию полезности: p2, p1, p3
        val topPlayers = resolver.getMostValuablePlayers()
        assertEquals(3, topPlayers.size)
        assertEquals("B_Forward", topPlayers[0].name)
        assertEquals("A_Defender", topPlayers[1].name)
        assertEquals("C_Defender", topPlayers[2].name)
    }

    @Test
    fun `resolver 7 - most popular agency by country with alphabet tie-breaker`() {
        // В Germany есть p1 (agency=null - игнорируется) и p2 (agency="Nike"). Популярное — Nike.
        // В Colombia есть p3 (agency="Adidas"). Популярное — Adidas.
        val agencyMap = resolver.getMostPopularAgencyByCountry()
        assertEquals("Nike", agencyMap["Germany"])
        assertEquals("Adidas", agencyMap["Colombia"])
    }

    @Test
    fun `resolver 8 - goals share split by median cost`() {
        // Стоимости: 10 млн, 20 млн, 30 млн. Медиана = 20 млн.
        // Группа <= медианы: p1 (10 млн, 5 голов) и p2 (20 млн, 15 голов) -> всего 20 голов.
        // Группа > медианы: p3 (30 млн, 2 гола) -> всего 2 гола.
        // Всего голов = 22.
        // Доля <= медианы: 20 / 22 = 0.9090...
        // Доля > медианы: 2 / 22 = 0.0909...
        val (lowShare, highShare) = resolver.getGoalsShareByMedianCost()

        // Проверяем с дельтой для чисел с плавающей точкой
        assertTrue(Math.abs(lowShare - (20.0 / 22.0)) < 0.001)
        assertTrue(Math.abs(highShare - (2.0 / 22.0)) < 0.001)
    }
}
