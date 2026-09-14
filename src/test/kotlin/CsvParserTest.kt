import model.Position
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CsvParserTest {

    @Test
    fun `empty agency maps to null`() {
        val lines = listOf(
            "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards",
            "Player 1;Team A;City A;FORWARD;USA;;1000;10;5;2;1;0"
        )
        val result = CsvParser.parseLines(lines)
        assertEquals(1, result.players.size)
        assertNull(result.players[0].agency)
    }

    @Test
    fun `broken lines are skipped and counted`() {
        val lines = listOf(
            "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards",
            "Valid;Team;City;FORWARD;USA;Agent;1000;10;5;2;1;0",
            "InvalidCost;Team;City;FORWARD;USA;Agent;NaN;10;5;2;1;0",
            ";;;;;;;;;;;"
        )
        val result = CsvParser.parseLines(lines)
        assertEquals(1, result.players.size)
        assertEquals(2, result.skippedCount)
    }

    @Test
    fun `position is case insensitive and ignores spaces`() {
        val lines = listOf(
            "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards",
            "P1;T;C;  forward  ;USA;A;1000;10;1;1;1;1",
            "P2;T;C;MIDFIELD;USA;A;1000;10;1;1;1;1",
            "P3;T;C;defender;USA;A;1000;10;1;1;1;1",
            "P4;T;C;  Goalkeeper  ;USA;A;1000;10;1;1;1;1"
        )
        val result = CsvParser.parseLines(lines)
        assertEquals(4, result.players.size)
        assertEquals(Position.FORWARD, result.players[0].position)
        assertEquals(Position.MIDFIELD, result.players[1].position)
        assertEquals(Position.DEFENDER, result.players[2].position)
        assertEquals(Position.GOALKEEPER, result.players[3].position)
    }
}