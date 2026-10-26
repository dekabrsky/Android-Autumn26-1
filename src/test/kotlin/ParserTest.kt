package parser

import model.Player
import model.Position
import kotlin.test.Test
import kotlin.test.assertEquals

class ParserTest {

    @Test
    fun `test parsing valid line`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            Iva Streich;Nevada whales;South Carolina;MIDFIELD;Colombia;D'Amore LLC;75012006;22;19;6;3;7
        """.trimIndent().lineSequence()

        val result = CsvParser.parse(csv)
        assertEquals(1, result.players.size)
        assertEquals(0, result.skippedCount)
        val player = result.players[0]
        assertEquals("Iva Streich", player.name)
        assertEquals(Position.MIDFIELD, player.position)
        assertEquals(75012006L, player.transferCost)
    }

    @Test
    fun `test parsing empty agency`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            Ms. Adolph Hartmann;North Carolina dolphins;Tennessee;FORWARD;Croatia;;52944545;27;10;5;5;8
        """.trimIndent().lineSequence()

        val result = CsvParser.parse(csv)
        assertEquals(1, result.players.size)
        assertEquals(null, result.players[0].agency)
    }

    @Test
    fun `test skipping invalid lines`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            Invalid;Line
            ;Empty;Name;FORWARD;Croatia;D'Amore LLC;52944545;27;10;5;5;8
            Valid;Team;City;FORWARD;Croatia;D'Amore LLC;52944545;27;10;5;5;8
        """.trimIndent().lineSequence()

        val result = CsvParser.parse(csv)
        assertEquals(1, result.players.size)
        assertEquals(2, result.skippedCount)
    }

    @Test
    fun `test position case insensitivity and trimming`() {
        val csv = """
            Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
            P1;T1;C1;  defender  ;N1;A1;100;10;5;5;1;1
            P2;T2;C2;FORWARD;N2;A2;200;20;10;10;2;2
        """.trimIndent().lineSequence()

        val result = CsvParser.parse(csv)
        assertEquals(2, result.players.size)
        assertEquals(Position.DEFENDER, result.players[0].position)
        assertEquals(Position.FORWARD, result.players[1].position)
    }
}
