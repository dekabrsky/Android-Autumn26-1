import filter.PlayerFilter.*
import model.Player
import model.Position
import model.Team
import parser.findFirstExpensiveFairPlayer
import report.report
import visualization.Charts
import kotlin.test.*
import java.io.Reader

class AdditionalTasksTest {
    private val player = Player("Alex", Team("A", "City"), Position.FORWARD, "Germany", null, 60_000_000, 25, 5, 2, 0, 0)

    @Test fun `aggregate calculates reusable totals and averages`() {
        val members = mutableListOf(player, player.copy(transferCost = 20, yellowCards = 4, redCards = 2))
        val team = Team.aggregate(members).single()
        members.clear()
        assertEquals(60_000_020L, team.totalTransferCost)
        assertEquals(2.0, team.averageYellowCards)
        assertEquals(1.0, team.averageRedCards)
        assertEquals(60_000_020L, team.totalTransferCost)
        assertEquals(0.0, Team("Empty", "City").averageRedCards)
    }

    @Test fun `recursive filters are exhaustive and price is strict`() {
        val forward = ByPosition(Position.FORWARD)
        val cheap = CheaperThan(60_000_000.0)
        assertTrue(forward.matches(player))
        assertFalse(cheap.matches(player))
        assertTrue(And(forward, Not(cheap)).matches(player))
        assertTrue(Or(cheap, forward).matches(player))
        assertFalse(Not(Or(cheap, forward)).matches(player))
        assertTrue(cheap.matches(player.copy(transferCost = 1)))
    }

    @Test fun `DSL renders metrics and nested sections`() {
        assertEquals("Игроков: 1\nАтаки\n  Голы: 5\n  Подробности\n    Передачи: 2", report {
            metric("Игроков") { 1 }
            section("Атаки") {
                metric("Голы") { player.goals }
                section("Подробности") { metric("Передачи") { player.assists } }
            }
        })
    }

    @Test fun `stream search skips broken rows and checks every boundary`() {
        val match = "Alex;A;City;FORWARD;Germany;;60000000;25;5;2;0;0"
        val lines = listOf("header", "broken", match.replace("60000000", "50000000"),
            match.replace(";25;", ";24;"), match.dropLast(1) + "1",
            match.replace(";0;0", ";1;0"), match, match.replace("Alex", "Later"))
        assertEquals("Alex", findFirstExpensiveFairPlayer(lines.joinToString("\n").reader())?.name)
        assertNull(findFirstExpensiveFairPlayer("header\nbroken".reader()))
    }

    @Test fun `stream search stops before consuming remainder and closes reader`() {
        val first = "Alex;A;City;FORWARD;Germany;;60000000;25;5;2;0;0\n"
        val reader = object : Reader() {
            var offset = 0
            var closed = false
            override fun read(buffer: CharArray, start: Int, length: Int): Int {
                check(offset < first.length) { "Search read beyond first matching row" }
                buffer[start] = first[offset++]
                return 1
            }
            override fun close() { closed = true }
        }
        assertNotNull(findFirstExpensiveFairPlayer(reader, hasHeader = false))
        assertTrue(reader.closed)
    }

    @Test fun `moving average sorts by price and averages both axes`() {
        val points = Charts.movingAverage(listOf(player.copy(transferCost = 30_000_000, goals = 9),
            player.copy(transferCost = 10_000_000, goals = 1), player.copy(transferCost = 20_000_000, goals = 5)), 2)
        assertEquals(listOf(Charts.Point(15.0, 3.0), Charts.Point(25.0, 7.0)), points)
        assertEquals(emptyList(), Charts.movingAverage(emptyList()))
        assertEquals(1, Charts.movingAverage(listOf(player)).size)
        assertFailsWith<IllegalArgumentException> { Charts.movingAverage(listOf(player), 0) }
    }
}
