import model.Position
import parser.parsePlayer
import parser.readCSV
import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

// Пример теста: так размечаются тесты и загружаются ресурсы из classpath.
// Дополните проект тестами на CsvParser (правила маппинга) и IResolver (задачи 1–8).
class InfrastructureTest {

    @Test
    fun `empty agency should become null`() {
        val line =
            "Ivan;Team A;City A;DEFENDER;Russia;;10;20;5;2;1;0"

        val player = parsePlayer(line)

        assertEquals(null, player?.agency)
    }

    @Test
    fun `invalid row should be skipped and counted`() {
        val csv = """
            name;team;city;position;nationality;agency;transferCost;participations;goals;assists;yellowCards;redCards
            Ivan;Team A;City A;DEFENDER;Russia;;10;20;5;2;1;0
            Broken;Team B;City B;DEFENDER;Russia;Agency;abc;20;5;2;1;0
            Alex;Team C;City C;FORWARD;Russia;Agency;20;15;4;3;0;0
        """.trimIndent()

        val inputStream = ByteArrayInputStream(csv.toByteArray())

        val result = readCSV(inputStream)

        assertEquals(2, result.players.size)
        assertEquals(1, result.skippedCount)
    }

    @Test
    fun `position should be recognized regardless of case and spaces`() {
        val line =
            "Ivan;Team A;City A;  dEfEnDeR  ;Russia;Agency;10;20;5;2;1;0"

        val player = parsePlayer(line)

        assertEquals(Position.DEFENDER, player?.position)
    }
}

