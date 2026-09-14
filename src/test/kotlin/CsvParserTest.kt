import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

import model.Position
import parser.CsvParser

class CsvParserTest {

    @Test
    fun `empty agency is mapped to null`() {
        val lines = listOf(
            "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards",
            "John Doe;Test Team;Berlin;FORWARD;Germany;;50000000;10;5;3;1;0"
        )

        val result = CsvParser.parse(lines)

        assertEquals(1, result.players.size)
        assertNull(result.players[0].agency)
    }

    @Test
    fun `broken rows are skipped and counted`() {
        val lines = listOf(
            "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards",
            "Valid Player;Test Team;Berlin;FORWARD;Germany;Agency A;50000000;10;5;3;1;0",
            "Broken Player;Test Team;Berlin;FORWARD;Germany;Agency A;not-a-number;10;5;3;1;0",
            "Another Valid;Test Team;Berlin;DEFENDER;Germany;;30000000;8;2;1;0;0"
        )

        val result = CsvParser.parse(lines)

        assertEquals(2, result.players.size)
        assertEquals(1, result.skippedCount)
    }

    @Test
    fun `positions are recognized regardless of letter case and spaces`() {
        val lines = listOf(
            "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards",
            "Player One;Team;City; forward ;Germany;Agency;1000000;1;1;1;0;0",
            "Player Two;Team;City;MIDFIELD;Germany;Agency;2000000;1;1;1;0;0",
            "Player Three;Team;City;DeFeNdEr;Germany;Agency;3000000;1;1;1;0;0"
        )

        val result = CsvParser.parse(lines)

        assertEquals(3, result.players.size)
        assertEquals(Position.FORWARD, result.players[0].position)
        assertEquals(Position.MIDFIELD, result.players[1].position)
        assertEquals(Position.DEFENDER, result.players[2].position)
    }
}