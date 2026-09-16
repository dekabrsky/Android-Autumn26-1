package parser

import model.Position
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertContentEquals

class CsvParserTest {

    private fun parse(vararg lines: String) =
        CsvParser.parse(lines.joinToString("\n").reader())

    private val header =
        "Name;Team;City;Position;Nationality;Agency;Transfer cost;" +
                "Participations;Goals;Assists;Yellow cards;Red cards"

    // 1. пустое агентство - null, игрок остаётся

    @Test
    fun `empty agency becomes null and player is kept`() {
        val result = parse(
            header,
            "Iva Streich;Nevada whales;South Carolina;FORWARD;Colombia;;75012006;22;19;6;3;7"
        )

        assertEquals(0, result.skippedCount)
        assertEquals(1, result.players.size)
        val p = result.players.single()
        assertNull(p.agency, "Пустое агентство должно стать null")
        assertEquals("Iva Streich", p.name)
        assertEquals("Nevada whales", p.team.name)
        assertEquals("South Carolina", p.team.city)
        assertEquals(Position.FORWARD, p.position)
        assertEquals("Colombia", p.country)
        assertEquals(75012006L, p.transferValue)
        assertEquals(22, p.matches)
        assertEquals(19, p.goals)
        assertEquals(6, p.assists)
        assertEquals(3, p.yellowCards)
        assertEquals(7, p.redCards)
    }

    @Test
    fun `non-empty agency keeps trimmed value`() {
        val result = parse(
            header,
            "  John Doe  ;  Alpha FC  ;  London  ;forward;England;  Big Agency  ;1000;10;5;2;1;0"
        )

        assertEquals(0, result.skippedCount)
        val p = result.players.single()
        assertEquals("John Doe", p.name)
        assertEquals("Alpha FC", p.team.name)
        assertEquals("London", p.team.city)
        assertEquals("Big Agency", p.agency)
    }

    // 2. Пропуск битых строк и подсчет

    @Test
    fun `empty name - row skipped and counted`() {
        val result = parse(
            header,
            ";Nevada whales;South Carolina;FORWARD;Colombia;;75012006;22;19;6;3;7"
        )
        assertEquals(0, result.players.size)
        assertEquals(1, result.skippedCount)
    }

    @Test
    fun `empty team - row skipped and counted`() {
        val result = parse(
            header,
            "Iva Streich;;South Carolina;FORWARD;Colombia;;75012006;22;19;6;3;7"
        )
        assertEquals(0, result.players.size)
        assertEquals(1, result.skippedCount)
    }

    @Test
    fun `empty city - row skipped and counted`() {
        val result = parse(
            header,
            "Iva Streich;Nevada whales;;FORWARD;Colombia;;75012006;22;19;6;3;7"
        )
        assertEquals(0, result.players.size)
        assertEquals(1, result.skippedCount)
    }

    @Test
    fun `empty country - row skipped and counted`() {
        val result = parse(
            header,
            "Iva Streich;Nevada whales;South Carolina;FORWARD;;;75012006;22;19;6;3;7"
        )
        assertEquals(0, result.players.size)
        assertEquals(1, result.skippedCount)
    }

    @Test
    fun `empty position - row skipped and counted`() {
        val result = parse(
            header,
            "Iva Streich;Nevada whales;South Carolina;;Colombia;;75012006;22;19;6;3;7"
        )
        assertEquals(0, result.players.size)
        assertEquals(1, result.skippedCount)
    }

    @Test
    fun `non-numeric transfer value - row skipped`() {
        val result = parse(
            header,
            "Iva Streich;Nevada whales;South Carolina;FORWARD;Colombia;Ag;abc;22;19;6;3;7"
        )
        assertEquals(0, result.players.size)
        assertEquals(1, result.skippedCount)
    }

    @Test
    fun `non-numeric goals - row skipped`() {
        val result = parse(
            header,
            "Iva Streich;Nevada whales;South Carolina;FORWARD;Colombia;Ag;1000;22;many;6;3;7"
        )
        assertEquals(0, result.players.size)
        assertEquals(1, result.skippedCount)
    }

    @Test
    fun `non-numeric assists - row skipped`() {
        val result = parse(
            header,
            "Iva Streich;Nevada whales;South Carolina;FORWARD;Colombia;Ag;1000;22;19;?;3;7"
        )
        assertEquals(0, result.players.size)
        assertEquals(1, result.skippedCount)
    }

    @Test
    fun `non-numeric yellow - row skipped`() {
        val result = parse(
            header,
            "Iva Streich;Nevada whales;South Carolina;FORWARD;Colombia;Ag;1000;22;19;6;x;7"
        )
        assertEquals(0, result.players.size)
        assertEquals(1, result.skippedCount)
    }

    @Test
    fun `non-numeric red - row skipped`() {
        val result = parse(
            header,
            "Iva Streich;Nevada whales;South Carolina;FORWARD;Colombia;Ag;1000;22;19;6;3;red"
        )
        assertEquals(0, result.players.size)
        assertEquals(1, result.skippedCount)
    }

    @Test
    fun `unknown position - row skipped`() {
        val result = parse(
            header,
            "Iva Streich;Nevada whales;South Carolina;GOALKEEPER_COACH;Colombia;Ag;1000;22;19;6;3;7"
        )
        assertEquals(0, result.players.size)
        assertEquals(1, result.skippedCount)
    }

    @Test
    fun `fewer columns than required - row skipped`() {
        val result = parse(
            header,
            "Iva Streich;Nevada whales;South Carolina"
        )
        assertEquals(0, result.players.size)
        assertEquals(1, result.skippedCount)
    }

    @Test
    fun `mixed batch counts skipped and kept correctly`() {
        val result = parse(
            header,
            "Alice;TeamA;CityA;FORWARD;CountryA;AgA;1000;10;5;2;1;0",
            "Bob;TeamB;CityB;DEFENDER;CountryB;;2000;20;0;0;2;0",
            ";TeamC;CityC;FORWARD;CountryC;AgC;3000;30;10;5;1;0",
            "Dan;TeamD;CityD;MIDFIELD;CountryD;AgD;not-a-number;5;1;0;0;0"
        )

        assertEquals(2, result.players.size)
        assertEquals(2, result.skippedCount)
        assertContentEquals(
            listOf("Alice", "Bob"),
            result.players.map { it.name }
        )
    }

    // 3 распознавание позиции в любом регистре

    @Test
    fun `position is case-insensitive and trimmed - data rows`() {
        val cases = listOf(
            "FORWARD"    to Position.FORWARD,
            "forward"    to Position.FORWARD,
            "  Forward " to Position.FORWARD,
            "DEFENDER"   to Position.DEFENDER,
            "defender"   to Position.DEFENDER,
            "MIDFIELD"   to Position.MIDFIELDER,
            "midfield"   to Position.MIDFIELDER,
            "GOALKEEPER" to Position.GOALKEEPER,
            "goalkeeper" to Position.GOALKEEPER
        )

        for ((token, expected) in cases) {
            val result = parse(
                header,
                "X;T;C;$token;Country;Ag;1000;10;0;0;0;0"
            )
            assertEquals(0, result.skippedCount, "token=$token")
            assertEquals(expected, result.players.single().position, "token=$token")
        }
    }

    //4 Прямая проверка fromCsvTokenOrNull

    @Test
    fun `Position_fromCsvTokenOrNull - case, spaces, synonyms`() {
        assertEquals(Position.FORWARD,    Position.fromCsvTokenOrNull("forward"))
        assertEquals(Position.FORWARD,    Position.fromCsvTokenOrNull("  FoRwArD  "))
        assertEquals(Position.FORWARD,    Position.fromCsvTokenOrNull("striker"))
        assertEquals(Position.DEFENDER,   Position.fromCsvTokenOrNull("DEF"))
        assertEquals(Position.MIDFIELDER, Position.fromCsvTokenOrNull("Mid"))
        assertEquals(Position.GOALKEEPER, Position.fromCsvTokenOrNull("gk"))
        assertEquals(Position.FORWARD,    Position.fromCsvTokenOrNull("нападающий"))
        assertEquals(Position.GOALKEEPER, Position.fromCsvTokenOrNull("вратарь"))
        assertNull(Position.fromCsvTokenOrNull("coach"))
        assertNull(Position.fromCsvTokenOrNull(""))
        assertNull(Position.fromCsvTokenOrNull("   "))
    }

    // 5 устойчивость к разделителю, кавычкам, пустым строкам

    @Test
    fun `semicolon delimiter detected from header`() {
        val result = parse(
            "Name;Team;City;Position;Nationality;Agency;Transfer cost;" +
                    "Participations;Goals;Assists;Yellow cards;Red cards",
            "Iva;T;C;FORWARD;Colombia;Ag;1000;10;5;2;1;0"
        )
        assertEquals(1, result.players.size)
        assertEquals(0, result.skippedCount)
    }

    @Test
    fun `comma delimiter also works`() {
        val result = parse(
            "Name,Team,City,Position,Nationality,Agency,Transfer cost," +
                    "Participations,Goals,Assists,Yellow cards,Red cards",
            "Iva,T,C,FORWARD,Colombia,Ag,1000,10,5,2,1,0"
        )
        assertEquals(1, result.players.size)
        assertEquals(0, result.skippedCount)
    }

    @Test
    fun `quoted field containing delimiter is preserved`() {
        val result = parse(
            header,
            "Iva;Nevada;South Carolina;FORWARD;Colombia;\"Agency, Inc.\";1000;10;5;2;1;0"
        )
        assertEquals(0, result.skippedCount)
        assertEquals("Agency, Inc.", result.players.single().agency)
    }

    @Test
    fun `blank lines are ignored, not counted as skipped`() {
        val result = parse(
            header,
            "",
            "   ",
            "Iva;T;C;FORWARD;Colombia;Ag;1000;10;5;2;1;0",
            "",
            "Bob;T;C;DEFENDER;Colombia;;1000;10;0;0;0;0"
        )
        assertEquals(2, result.players.size)
        assertEquals(0, result.skippedCount)
    }

    @Test
    fun `numbers with spaces are cleaned before parsing`() {
        val result = parse(
            header,
            "Iva;T;C;FORWARD;Colombia;Ag;1 000 000;10;5;2;1;0"
        )
        assertEquals(0, result.skippedCount)
        assertEquals(1_000_000L, result.players.single().transferValue)
    }

    @Test
    fun `empty file yields empty result with zero skips`() {
        val result = parse()
        assertTrue(result.players.isEmpty())
        assertEquals(0, result.skippedCount)
    }

    @Test
    fun `only header yields empty result with zero skips`() {
        val result = parse(header)
        assertTrue(result.players.isEmpty())
        assertEquals(0, result.skippedCount)
    }

    // 6 Позиция не null после парсинга

    @Test
    fun `all parsed players have non-null position and non-empty name`() {
        val result = parse(
            header,
            "Alice;TeamA;CityA;FORWARD;CountryA;AgA;1000;10;5;2;1;0",
            "Bob;TeamB;CityB;defender;CountryB;;2000;20;0;0;2;0",
            "Carol;TeamC;CityC;  MidField  ;CountryC;AgC;3000;30;0;0;0;0"
        )
        assertEquals(3, result.players.size)
        result.players.forEach { p ->
            assertNotNull(p.position)
            assertTrue(p.name.isNotBlank())
            assertTrue(p.team.name.isNotBlank())
            assertTrue(p.team.city.isNotBlank())
            assertTrue(p.country.isNotBlank())
        }
    }
}