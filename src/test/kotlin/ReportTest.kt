import report.report
import kotlin.test.Test
import kotlin.test.assertEquals

class ReportTest {
    @Test
    fun `report renders metric`() {
        val result = report {
            metric("Без агентства") { 2 }
        }

        assertEquals("Без агентства: 2", result)
    }

    @Test
    fun `report renders section with nested metrics`() {
        val result = report {
            metric("Без агентства") { 2 }
            section("Атаки") {
                metric("Всего голов") { 34 }
            }
        }

        assertEquals(
            listOf(
                "Без агентства: 2",
                "Атаки",
                "  Всего голов: 34",
            ).joinToString(System.lineSeparator()),
            result,
        )
    }

    @Test
    fun `report supports nested sections`() {
        val result = report {
            section("Команды") {
                section("Nevada whales") {
                    metric("Игроков") { 3 }
                }
            }
        }

        assertEquals(
            listOf(
                "Команды",
                "  Nevada whales",
                "    Игроков: 3",
            ).joinToString(System.lineSeparator()),
            result,
        )
    }
}
