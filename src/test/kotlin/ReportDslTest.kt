import report.report
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReportDslTest {

    @Test
    fun `report renders title metrics and nested sections`() {
        val text = report("Чемпионат") {
            metric("Без агентства") { 2 }
            section("Атаки") {
                metric("Всего голов") { 40 }
                metric("Лучший") { "Charlie" }
            }
        }.render()

        val lines = text.lines()

        assertEquals("Чемпионат", lines[0])
        assertTrue(text.contains("Без агентства: 2"), text)
        assertTrue(text.contains("[Атаки]"), text)
        assertTrue(text.contains("  Всего голов: 40"), text)
        assertTrue(text.contains("  Лучший: Charlie"), text)
    }

    @Test
    fun `metric values are computed lazily on render`() {
        var calls = 0

        val built = report("Ленивость") {
            metric("Счётчик") { calls++ }
        }

        assertEquals(0, calls)

        built.render()
        assertEquals(1, calls)
    }

    @Test
    fun `numbers and collections are formatted`() {
        val text = report("Формат") {
            metric("Дробное") { 1234.5 }
            metric("Целое") { 1_000_000L }
            metric("Список") { listOf(1, 2, 3) }
            metric("Ничего") { null }
        }.render()

        assertTrue(text.contains("Дробное: 1,234.50"), text)
        assertTrue(text.contains("Целое: 1,000,000"), text)
        assertTrue(text.contains("Список: 1, 2, 3"), text)
        assertTrue(text.contains("Ничего: нет данных"), text)
    }
}
