import filter.PlayerFilter
import filter.and
import filter.bestDefenderScorer
import filter.not
import filter.or
import filter.select
import model.Position
import model.toTeams
import parser.CsvParser
import report.report
import resolver.Resolver
import task.findFirstCleanStar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ExtraTasksTest {
    private val cheapDef = player("A", position = Position.DEFENDER, cost = 10, goals = 4)
    private val richDef = player("B", position = Position.DEFENDER, cost = 500, goals = 9)
    private val keeper = player("C", position = Position.GOALKEEPER, cost = 50)
    private val all = listOf(cheapDef, richDef, keeper)

    @Test
    fun `filters combine`() {
        val def = PlayerFilter.ByPosition(Position.DEFENDER)
        val cheap = PlayerFilter.CheaperThan(100)
        assertEquals(listOf(cheapDef), all.select(def and cheap))
        assertEquals(listOf(cheapDef, keeper), all.select(cheap))
        assertEquals(listOf(cheapDef, richDef), all.select(def or cheap).filter { it.position == Position.DEFENDER })
        assertEquals(listOf(richDef, keeper), all.select(!(def and cheap)))
    }

    @Test
    fun `query 2 via filters equals resolver`() {
        assertEquals(Resolver(all).getBestScorerDefender(), bestDefenderScorer(all))
    }

    @Test
    fun `team lazy aggregates`() {
        val team = all.toTeams().single()
        assertEquals(560L, team.totalTransferCost)
        assertEquals(0.0, team.averageCards)
    }

    @Test
    fun `report dsl renders sections`() {
        val text = report(all) {
            metric("Игроков") { players.size }
            section("Атаки") {
                metric("Голов") { players.sumOf { it.goals } }
            }
        }.render()
        assertEquals("Игроков: 3\n== Атаки ==\n  Голов: 13\n", text)
    }

    @Test
    fun `streaming search agrees with in memory search`() {
        val expected = CsvParser.parseResource("fakePlayers.csv").players.firstOrNull {
            it.transferCost > 50_000_000 && it.appearances >= 25 && it.yellowCards == 0 && it.redCards == 0
        }
        assertEquals(expected, findFirstCleanStar("fakePlayers.csv"))
        expected?.let { assertTrue(it.transferCost > 50_000_000) }
    }
}
