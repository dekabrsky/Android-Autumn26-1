import kotlin.test.Test
import kotlin.test.assertNotNull
import model.Player
import model.Position
import model.Team
import org.junit.jupiter.api.Assertions.*
import parser.CsvParser
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

    @Test
    fun `test parser mapping rules and skipped strings`() {
        val parser = CsvParser()
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            Игрок1;КомандаА;ГородА; ForWaRd ;Страна1;; 100 000 ;10;5;2;0;0
            Игрок2;КомандаА;ГородА;defender;Страна1;Агентство1;200000;10;5;2;0;0
            НехваткаКолонок;Команда;Город;FORWARD
            ПлохоеЧисло;Команда;Город;FORWARD;Страна;Агент;СтоТыщ;10;5;2;0;0
            ;ПустоеИмя;Город;FORWARD;Страна;Агент;100000;10;5;2;0;0
        """.trimIndent().lines().asSequence()

        val result = parser.parse(csv)

        // 1. Проверка пропуска "битых" строк: 2 хороших, 3 плохих
        assertEquals(2, result.players.size, "Должно быть 2 успешных игрока")
        assertEquals(3, result.skippedCount, "Должно быть 3 пропущенные строки")

        // 2. Распознавание позиций в любом регистре и с пробелами
        assertEquals(Position.FORWARD, result.players[0].position, "Позиция должна нормализоваться")
        assertEquals(Position.DEFENDER, result.players[1].position)

        // 3. Пустое агентство -> null
        assertNull(result.players[0].agency, "Пустое агентство должно быть null")
        assertEquals("Агентство1", result.players[1].agency)
    }

    @Test
    fun `test resolver queries`() {
        val teamA = Team("КомандаА", "ГородА") // Всего КК = 1 (в ср. 0.5)
        val teamB = Team("КомандаБ", "ГородБ") // Всего КК = 5 (в ср. 2.5) -> Самая грубая

        val p1 = Player("Борис", teamA, Position.FORWARD, "Германия", null, 100, 10, 5, 0, 0, 1)
        val p2 = Player("Анна", teamA, Position.DEFENDER, "Germany", "Ag1", 200, 10, 3, 0, 0, 0)
        val p3 = Player("Виктор", teamB, Position.FORWARD, "Brazil", "Ag1", 100, 10, 10, 10, 0, 2)
        val p4 = Player("Дмитрий", teamB, Position.DEFENDER, "Brazil", "Ag2", 300, 10, 1, 0, 0, 3)

        // Добавляем игроков в агрегаты (т.к. они собираются в парсере, здесь в тесте связываем руками)
        teamA.players.apply { add(p1); add(p2) }
        teamB.players.apply { add(p3); add(p4) }

        val players = listOf(p1, p2, p3, p4)
        val resolver = Resolver(players)

        // 1. Без агентства (Только Борис)
        assertEquals(1, resolver.getCountWithoutAgency())

        // 2. Лучший защ-бомбардир (Анна - 3 гола, Дмитрий - 1)
        assertEquals("Анна" to 3, resolver.getBestScorerDefender())

        // 3. Позиция самого дорогого немца (Германия/Germany). Анна(200) > Борис(100) -> защитник
        assertEquals("защитник", resolver.getTheExpensiveGermanPlayerPosition())

        // 4. Самая грубая команда
        assertEquals(teamB, resolver.getTheRudestTeam())

        // 5. Средняя стоимость по позициям (DEFENDER=(200+300)/2=250, FORWARD=(100+100)/2=100)
        val costByPos = resolver.getAverageTransferCostByPosition()
        val expectedCosts = mapOf(Position.DEFENDER to 250.0, Position.FORWARD to 100.0)
        assertEquals(expectedCosts, costByPos)
        assertEquals(Position.DEFENDER, costByPos.keys.first(), "Сортировка по убыванию")

        // 6. Топ-3 MVP (Виктор: 10+20=30, Борис: 5+0=5, Анна: 3+0=3, Дмитрий: 1)
        val top3 = resolver.getMostValuablePlayers()
        assertEquals(listOf(p3, p1, p2), top3)

        // 7. Популярные агентства. Brazil -> Ag1 и Ag2 по 1, по алфавиту Ag1. Германия -> Ag1 (т.к. у Бориса null)
        val agencies = resolver.getMostPopularAgencyByCountry()
        assertEquals("Ag1", agencies["Brazil"])
        assertEquals("Ag1", agencies["Germany"])

        // 8. Доля голов. Медиана (100,100,200,300) = 150.
        // Дешевые (<= 150): Борис(5), Виктор(10). Итого 15.
        // Дорогие (> 150): Анна(3), Дмитрий(1). Итого 4. Всего голов 19.
        val (cheapShare, expShare) = resolver.getGoalsShareByMedianCost()
        assertEquals(15.0 / 19.0, cheapShare, 0.001)
        assertEquals(4.0 / 19.0, expShare, 0.001)
    }
}