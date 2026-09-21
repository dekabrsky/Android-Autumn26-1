import Denisova1dz.model.Player
import Denisova1dz.model.Position
import Denisova1dz.model.Team
import Denisova1dz.parser.CsvParser
import Denisova1dz.resolver.Resolver
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Пример теста: так размечаются тесты и загружаются ресурсы из classpath.
// Дополните проект тестами на CsvParser (правила маппинга) и IResolver (задачи 1–8).
class InfrastructureTest {

    @Test
    fun `dirty dataset is available on classpath`() {
        //что файл "плохого" датасета найден в ресурсах и метод загрузки вернул не null
        assertNotNull(javaClass.classLoader.getResourceAsStream("fakePlayersDirty.csv"))
    }

    @Test
    fun `clean dataset for tests is available on classpath`() {
        // что файл "чистого" датасета найден в ресурсах и метод загрузки вернул не null
        assertNotNull(javaClass.classLoader.getResourceAsStream("fakePlayers.csv"))
    }

    // Тесты на парсер
    //тест на то, что в таблице ячейки agency пустые, а в Player они null
    @Test
    fun `empty agency is mapped to null`() {
        val resource = javaClass.classLoader.getResource("fakePlayers.csv") //находим файл fakePlayers.csv в папке ресурсов проекта и получаем ссылку на его местоположение
        assertNotNull(resource) //проверяем, что файл существует и загрузился

        // отдаем файл парсеру CsvParser, чтобы он считал игроков
        val result = CsvParser.parse(File(resource.file).absolutePath)

        // Ищем среди всех прочитанных игроков любого, у кого нет агентства
        val playerWithNoAgency = result.players.find { it.agency == null }
        //убеждаемся, что у этого игрока агентство РАВНО null
        assertNull(playerWithNoAgency?.agency)
    }
    //тест на некорректные строки и их подсчет
    @Test
    fun `dirty dataset skips bad rows and counts them`() {
        val resource = javaClass.classLoader.getResource("fakePlayersDirty.csv") //загружает файл «грязного» датасета
        assertNotNull(resource) //файл существует

        val result = CsvParser.parse(File(resource.file).absolutePath) //запускает парсер, внутри срабатывают проверки toIntOrNull(), toLongOrNull() и проверка количества колонок

        // проверяем, что пропущенные строки посчитаны (больше 0)
        assertTrue(result.skippedCount > 0)
    }

    @Test
    fun `position case insensitivity check`() {
        // Проверка требования распознавания позиций в любом регистре
        assertEquals(
            Position.FORWARD,
            Position.fromCsvTokenOrNull("forward")
        ) //текст из маленьких букв "forward" корректно
        assertEquals(
            Position.FORWARD,
            Position.fromCsvTokenOrNull("FORWARD")
        ) //текст из заглавных букв "FORWARD" корректно
        assertEquals(
            Position.MIDFIELD,
            Position.fromCsvTokenOrNull("  midfield ")
        ) //скрытые пробелы в начале и в конце строки удаляются
    }

    // Тесты на IResolver
    // тестовый список из 3 игроков
    private val testPlayers = listOf(
        Player(
            name = "Player 1",
            team = Team("Nevada whales", "South Carolina"),
            position = Position.FORWARD,
            country = "Spain",
            agency = "Agency A",
            transfer = 100L,
            matches = 10,
            goals = 5,
            assists = 2,
            yellowCards = 1,
            redCards = 0
        ),
        Player(
            name = "Player 2",
            team = Team("Mississippi witches", "Virginia"),
            position = Position.MIDFIELD,
            country = "Spain",
            agency = "Agency A",
            transfer = 200L,
            matches = 15,
            goals = 1,
            assists = 4,
            yellowCards = 2,
            redCards = 0
        ),
        Player(
            name = "Player 3",
            team = Team("Utah nemesis", "New York"),
            position = Position.FORWARD,
            country = "Brazil",
            agency = null,
            transfer = 300L,
            matches = 20,
            goals = 10,
            assists = 3,
            yellowCards = 0,
            redCards = 1
        )
    )

    private val resolver = Resolver(testPlayers)
    //правильность подсчёта игроков, у которых нет агентства или пустая строка
    @Test
    fun `test getCountWithoutAgency`() {
        assertEquals(1, resolver.getCountWithoutAgency()) //обращается к списку из 3 человек, считает количество игроков без агентства и возвращает результат
        //сравнивает ожидаемое значение (1) с тем, что реально вернула функция
    }
    //правильно ли программа определяет самое популярное агентство для каждой страны
    @Test
    fun `test getMostPopularAgencyByCountry`() {
        val result = resolver.getMostPopularAgencyByCountry() //группирует игроков по странам, находит лидера для каждой страны
        // возвращает словарь, где ключ — страна, значение — самое популярное агентство
        assertEquals("Agency A", result["Spain"]) //берёт из полученного словаря результат для страны "Spain" и сверяет его с ожидаемой строкой "Agency A"
    }
}