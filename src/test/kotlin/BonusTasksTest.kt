import filter.PlayerFilter
import filter.filterBy
import model.Player
import model.Position
import model.Team
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BonusTasksTest {

    private val team = Team("Test", "City")

    private fun player(
        name: String = "P",
        position: Position = Position.FORWARD,
        cost: Long = 1_000_000,
        matches: Int = 10,
        yellow: Int = 0,
        red: Int = 0
    ) = Player(
        name = name,
        team = team,
        position = position,
        nationality = "Germany",
        agency = null,
        transferCost = cost,
        participations = matches,
        goals = 1,
        assists = 0,
        yellowCards = yellow,
        redCards = red
    )

    @Test
    fun `streaming search finds first matching player from stream`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            Too Cheap;Team A;City A;FORWARD;Germany;Agency;1000;30;1;0;0;0
            Has Cards;Team A;City A;FORWARD;Germany;Agency;60000000;30;1;0;2;0
            Few Matches;Team A;City A;FORWARD;Germany;Agency;60000000;10;1;0;0;0
            Good One;Team A;City A;MIDFIELD;Germany;Agency;55000000;28;5;1;0;0
            Another;Team A;City A;FORWARD;Germany;Agency;70000000;30;1;0;0;0
        """.trimIndent()

        val found = CsvParser.findFirstCleanExpensivePlayer(csv.byteInputStream())
        assertNotNull(found)
        assertEquals("Good One", found!!.name)
    }

    @Test
    fun `streaming search returns null when nobody matches`() {
        val found = CsvParser.findFirstCleanExpensivePlayer("fakePlayersDirty.csv")
        // в выданном датасете такого игрока нет — это нормально для firstOrNull
        assertNull(found)
    }

    @Test
    fun `by position and cheaper than filters`() {
        val list = listOf(
            player("A", Position.DEFENDER, 30_000_000),
            player("B", Position.DEFENDER, 50_000_000),
            player("C", Position.FORWARD, 20_000_000)
        )

        val filter = PlayerFilter.And(
            PlayerFilter.ByPosition(Position.DEFENDER),
            PlayerFilter.CheaperThan(40_000_000)
        )

        val result = list.filterBy(filter)
        assertEquals(listOf("A"), result.map { it.name })
    }

    @Test
    fun `or and not filters`() {
        val cheapForward = player("F", Position.FORWARD, 5_000_000)
        val expensiveDefender = player("D", Position.DEFENDER, 60_000_000)

        val filter = PlayerFilter.Or(
            PlayerFilter.ByPosition(Position.FORWARD),
            PlayerFilter.Not(PlayerFilter.CheaperThan(50_000_000))
        )

        assertTrue(filter.matches(cheapForward))
        assertTrue(filter.matches(expensiveDefender))
        assertFalse(filter.matches(player("X", Position.MIDFIELD, 10_000_000)))
    }
}
